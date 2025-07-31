#!/bin/bash
set -e

PASS="changeit"
KEY_PASS="changeit"

echo "--- Creating Root Certificate Authority ---"
openssl genrsa -out ca-key.pem 2048
openssl req -new -x509 -key ca-key.pem -out ca-cert.pem -days 3650 -subj "/C=NL/ST=NH/L=Amsterdam/O=BankApp/OU=Security/CN=BankAppRootCA"

echo "--- Creating shared Truststore (JKS) and importing Root Certificate Authority ---"
keytool -noprompt -import -trustcacerts -alias ca-root-cert -file ca-cert.pem -keystore kafka.truststore.jks -storepass "$PASS" -storetype JKS
echo "✅ Certificate Authority and shared Truststore with Root Certificate Authority generated"

BROKERS=("kafka-1" "kafka-2" "kafka-3")

for BROKER_NAME in "${BROKERS[@]}"; do
    echo "--- Creating JKS certificates for broker: $BROKER_NAME ---"
    keytool -noprompt -genkeypair -alias "$BROKER_NAME" -keystore "$BROKER_NAME.keystore.jks" -keyalg RSA -keysize 2048 -validity 3650 -storepass "$PASS" -keypass "$KEY_PASS" -dname "CN=$BROKER_NAME, OU=Kafka, O=BankApp, L=Amsterdam, C=NL" -ext "SAN=DNS:$BROKER_NAME,DNS:localhost" -storetype JKS
    keytool -noprompt -certreq -alias "$BROKER_NAME" -keystore "$BROKER_NAME.keystore.jks" -file "$BROKER_NAME.csr" -storepass "$PASS"
    openssl x509 -req -CA ca-cert.pem -CAkey ca-key.pem -in "$BROKER_NAME.csr" -out "$BROKER_NAME-signed.pem" -days 3650 -CAcreateserial -passin pass:"$PASS"
    keytool -noprompt -import -trustcacerts -alias ca-root-cert -file ca-cert.pem -keystore "$BROKER_NAME.keystore.jks" -storepass "$PASS" -storetype JKS
    keytool -noprompt -import -alias "$BROKER_NAME" -file "$BROKER_NAME-signed.pem" -keystore "$BROKER_NAME.keystore.jks" -storepass "$PASS" -storetype JKS
    keytool -noprompt -import -trustcacerts -alias "broker-$BROKER_NAME-public" -file "$BROKER_NAME-signed.pem" -keystore kafka.truststore.jks -storepass "$PASS" -storetype JKS
    echo "✅ Certificates for $BROKER_NAME generated"
done

CLIENT_NAME="bank-api-client"
echo "--- Creating JKS certificates for client: $CLIENT_NAME ---"
keytool -noprompt -genkeypair -alias "$CLIENT_NAME" -keystore "$CLIENT_NAME.keystore.jks" -keyalg RSA -keysize 2048 -validity 3650 -storepass "$PASS" -keypass "$KEY_PASS" -dname "CN=$CLIENT_NAME, OU=Clients, O=BankApp, L=Amsterdam, C=NL" -storetype JKS
keytool -noprompt -certreq -alias "$CLIENT_NAME" -keystore "$CLIENT_NAME.keystore.jks" -file "$CLIENT_NAME.csr" -storepass "$PASS"
openssl x509 -req -CA ca-cert.pem -CAkey ca-key.pem -in "$CLIENT_NAME.csr" -out "$CLIENT_NAME-signed.pem" -days 3650 -CAcreateserial -passin pass:"$PASS"
keytool -noprompt -import -trustcacerts -alias ca-root-cert -file ca-cert.pem -keystore "$CLIENT_NAME.keystore.jks" -storepass "$PASS" -storetype JKS
keytool -noprompt -import -alias "$CLIENT_NAME" -file "$CLIENT_NAME-signed.pem" -keystore "$CLIENT_NAME.keystore.jks" -storepass "$PASS" -storetype JKS

echo "--- Adding client's public certificate to kafka.truststore.jks ---"
keytool -noprompt -import -trustcacerts -alias "client-$CLIENT_NAME-public" -file "$CLIENT_NAME-signed.pem" -keystore kafka.truststore.jks -storepass "$PASS" -storetype JKS

echo "--- ✨ All JKS certificates successfully created ---"