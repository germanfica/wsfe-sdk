package com.germanfica.wsfe.utils;

import org.apache.commons.codec.binary.Base64;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class CryptoUtils {

    public static PrivateKey loadPrivateKey(String keystorePath, String password, String alias) {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(new FileInputStream(keystorePath), password.toCharArray());
            return (PrivateKey) keystore.getKey(alias, password.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar la clave privada", e);
        }
    }

    public static X509Certificate loadCertificate(String keystorePath, String password, String alias) {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(new FileInputStream(keystorePath), password.toCharArray());
            return (X509Certificate) keystore.getCertificate(alias);
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar el certificado", e);
        }
    }

    public static String encodeBase64(byte[] data) {
        return Base64.encodeBase64String(data);
    }
}
