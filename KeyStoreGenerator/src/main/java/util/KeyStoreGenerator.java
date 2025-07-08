package util;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
public class KeyStoreGenerator {
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_FILE = "config/app-secrets.p12";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String AES_ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final String JWT_KEY_ALIAS = "JWT_KEY_ALIAS";
    private static final String CARD_KEY_ALIAS = "CARD_KEY_ALIAS";
    private static final String REFRESH_KEY_ALIAS = "REFRESH_KEY_ALIAS";
    private static final String ENCRYPTION_KEYSTORE_PASSWORD = "ENCRYPTION_KEYSTORE_PASSWORD";

    static {
        System.setProperty("keystore.pkcs12.legacy", "true");
    }

    public static void main(String[] args) {
        try {
            generateKeystoreFile();
            log.info("Keystore generated",
                    keyValue("timestamp", LocalDateTime.now())
            );
        } catch (Exception e) {
            log.error("Failed to generate keystore",
                    keyValue("error_type", e.getClass().getSimpleName()),
                    keyValue("error_message", e.getMessage()), e
            );
            throw new IllegalStateException("Keystore generation failed", e);
        }
    }

    private static String getEnv(String name) {
        return Optional.ofNullable(System.getenv(name))
                .filter(env -> !env.trim().isEmpty())
                .orElseThrow(() -> new IllegalStateException(name + " environment variable is not set or empty"));
    }

    private static void generateKeystoreFile()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        String jwtKeyAlias = getEnv(JWT_KEY_ALIAS);
        String cardKeyAlias = getEnv(CARD_KEY_ALIAS);
        String refreshKeyAlias = getEnv(REFRESH_KEY_ALIAS);
        char[] keystorePassword = getEnv(ENCRYPTION_KEYSTORE_PASSWORD).toCharArray();

        try {
            KeyStore keystore = createEmptyKeystore(keystorePassword);

            addKeyToKeystore(keystore, jwtKeyAlias,
                    generateKey(HMAC_ALGORITHM, KEY_SIZE), keystorePassword);
            addKeyToKeystore(keystore, cardKeyAlias,
                    generateKey(AES_ALGORITHM, KEY_SIZE), keystorePassword);
            addKeyToKeystore(keystore, refreshKeyAlias,
                    generateKey(AES_ALGORITHM, KEY_SIZE), keystorePassword);

            saveKeystoreToFile(keystore, keystorePassword);
        } finally {
            Arrays.fill(keystorePassword, '\0');
        }
    }

    private static KeyStore createEmptyKeystore(char[] password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystore.load(null, password);
        log.info("Empty keystore created",
                keyValue("keystore_type", KEYSTORE_TYPE)
        );
        return keystore;
    }

    private static void addKeyToKeystore(KeyStore keystore, String alias,
                                         SecretKey key, char[] password) throws KeyStoreException {
        Objects.requireNonNull(alias, "Key alias cannot be null");
        Objects.requireNonNull(key, "Key cannot be null");

        KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);
        keystore.setEntry(alias, new KeyStore.SecretKeyEntry(key), param);
        log.info("Key added to keystore",
                keyValue("alias", alias),
                keyValue("algorithm", key.getAlgorithm()),
                keyValue("format", key.getFormat())
        );
    }

    private static void saveKeystoreToFile(KeyStore keystore, char[] password)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        try (FileOutputStream fos = new FileOutputStream(KEYSTORE_FILE)) {
            keystore.store(fos, password);
            log.info("Keystore saved to file",
                    keyValue("file_path", KEYSTORE_FILE)
            );
        }
    }

    private static SecretKey generateKey(String algorithm, int keySize) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
            keyGen.init(keySize);
            log.info("Generated key",
                    keyValue("algorithm", algorithm)
            );
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to initialize key generator for algorithm: {}", algorithm,
                    keyValue("error_type", e.getClass().getSimpleName()),
                    keyValue("error_message", e.getMessage()), e);
            throw new IllegalStateException("Unsupported algorithm: " + algorithm, e);
        }
    }
}