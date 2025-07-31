#!/bin/bash
set -e

if [ ! -f "ca-key.pem" ]; then
    echo "Error: The ca-key.pem file was not found. Please run the script for Kafka first"
    exit 1
fi

NODES=("redis-node-1" "redis-node-2" "redis-node-3" "redis-node-4" "redis-node-5" "redis-node-6")
for NODE_NAME in "${NODES[@]}"; do
    echo "--- Generating PEM certificates for Redis node: $NODE_NAME ---"
    openssl genrsa -out $NODE_NAME.key.pem 2048
    openssl req -new -key $NODE_NAME.key.pem -out $NODE_NAME.csr -subj "/C=NL/ST=NH/L=Amsterdam/O=BankApp/OU=Redis/CN=$NODE_NAME"
    openssl x509 -req -CA ca-cert.pem -CAkey ca-key.pem -in $NODE_NAME.csr -out $NODE_NAME.crt.pem -days 3650 -CAcreateserial -extfile <(printf "subjectAltName=DNS:$NODE_NAME,DNS:localhost")
done

CLIENT_NAME="bank-api-redis-client"
echo "--- Generating PEM certificates for Redis client: $CLIENT_NAME ---"
openssl genrsa -out $CLIENT_NAME.key.pem 2048
openssl req -new -key $CLIENT_NAME.key.pem -out $CLIENT_NAME.csr -subj "/C=NL/ST=NH/L=Amsterdam/O=BankApp/OU=Clients/CN=$CLIENT_NAME" -passin pass:$CLIENT_KEY_PASS
openssl x509 -req -CA ca-cert.pem -CAkey ca-key.pem -in $CLIENT_NAME.csr -out $CLIENT_NAME.crt.pem -days 3650 -CAcreateserial

echo "--- ✨ All PEM certificates for Redis have been successfully generated ---"