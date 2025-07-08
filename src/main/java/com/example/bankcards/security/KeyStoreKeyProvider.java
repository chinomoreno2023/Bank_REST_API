package com.example.bankcards.security;

import com.example.bankcards.config.security.EncryptionProperties;
import com.example.bankcards.exception.security.KeyStoreLoadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Component
@RequiredArgsConstructor
public class KeyStoreKeyProvider {
    private final EncryptionProperties properties;

    public SecretKey loadKey(String alias) {
        try (InputStream inputStream = new FileInputStream(properties.getKeystorePath())) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(inputStream, properties.getKeystorePassword().toCharArray());

            Key key = keyStore.getKey(alias, properties.getKeystorePassword().toCharArray());
            if (key instanceof SecretKey secretKey) {
                return secretKey;
            } else {
                throw new KeyStoreLoadException("Key in keystore is not a secret key");
            }
        } catch (FileNotFoundException e) {
            throw new KeyStoreLoadException("Keystore file not found", e);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreLoadException("Keystore algorithm not supported", e);
        } catch (CertificateException e) {
            throw new KeyStoreLoadException("Certificate error in keystore", e);
        } catch (UnrecoverableKeyException e) {
            throw new KeyStoreLoadException("Failed to recover key (wrong password?)", e);
        } catch (KeyStoreException | IOException e) {
            throw new KeyStoreLoadException("Failed to load keystore", e);
        } catch (Exception e) {
            throw new KeyStoreLoadException("Unexpected keystore error", e);
        }
    }
}
