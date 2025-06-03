package com.germanfica.wsfe.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CryptoUtils {
    /**
     * Static block for registering the Bouncy Castle ("BC") security provider.
     *
     * <p>This SDK uses Bouncy Castle to sign CMS (Cryptographic Message Syntax) data
     * required by the AFIP/ARCA WSAA SOAP authentication flow.</p>
     *
     * <p>Since the SDK is designed to be lightweight and not depend on external frameworks
     * like Spring or application servers, the provider is registered automatically
     * when this class is loaded. This simplifies integration for the developer by
     * eliminating the need for manual setup.</p>
     *
     * <p>The provider is only added if it has not been registered already, which prevents
     * conflicts with existing configurations. The registration is scoped to the JVM runtime
     * and lasts only for the duration of the process, making it safe for CLI tools,
     * ephemeral SDK usage, and isolated executions.</p>
     *
     * <p>Note: If this SDK is used in a more complex or persistent environment (e.g. servlet container,
     * application server, or modular JVM), the initialization strategy may need to be reviewed.</p>
     *
     * Registers the Bouncy Castle security provider if not already present.
     *
     * <p>This static block ensures that the Bouncy Castle cryptographic provider is
     * available in the JVM at runtime, which is required for certain advanced encryption
     * algorithms used by this SDK.</p>
     *
     * <p><strong>Note:</strong> The provider is registered globally via
     * {@link java.security.Security#addProvider(java.security.Provider)}, meaning it
     * becomes accessible to the entire application, including other libraries and frameworks
     * like Spring. This is a safe and common practice, but if you prefer to manage security
     * providers explicitly or avoid global side effects, you can remove this block and
     * register the provider manually.</p>
     *
     * <p>To register Bouncy Castle manually before using this SDK:</p>
     *
     * <pre>{@code
     * import org.bouncycastle.jce.provider.BouncyCastleProvider;
     * import java.security.Security;
     *
     * public class Application {
     *     public static void main(String[] args) {
     *         if (Security.getProvider("BC") == null) {
     *             Security.addProvider(new BouncyCastleProvider());
     *         }
     *
     *         // Now you can use the SDK safely
     *     }
     * }
     * }</pre>
     *
     * @see java.security.Security
     * @see org.bouncycastle.jce.provider.BouncyCastleProvider
     */
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

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
    public static byte[] decodeBase64(String base64) {
        return Base64.decodeBase64(base64);
    }

    /**
     * Extrae un {@link X509Certificate} a partir de los bytes que representan
     *     - un certificado X.509 DER        (----- CmsFormatInspector.isCertificate(...))
     *     - un CMS firmado (SignedData)     (----- CmsFormatInspector.isCmsSigned(...))
     *
     * @param data contenido binario (no codificado)
     * @return el certificado embebido
     * @throws IllegalArgumentException si <i>data</i> no es ni certificado ni CMS
     */
    public static X509Certificate extractCertificate(byte[] data) {
        if (CmsFormatInspector.isCertificate(data)) return toCertificate(data);

        if (CmsFormatInspector.isCmsSigned(data)) {
            try {
                CMSSignedData cms = new CMSSignedData(data);
                return new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(
                        (org.bouncycastle.cert.X509CertificateHolder)
                            cms.getCertificates()
                                .getMatches(cms.getSignerInfos().getSigners().iterator().next().getSID())
                                .iterator().next()
                    );
            } catch (Exception e) {
                throw new RuntimeException("Error extrayendo certificado desde CMS", e);
            }
        }

        throw new IllegalArgumentException("Contenido no reconocido como certificado ni CMS");
    }

    public static X509Certificate extractCertificate(String base64) {
        return extractCertificate(decodeBase64(base64));
    }

    /**
     * Convierte un array de bytes en un {@link X509Certificate}.
     *
     * @param certBytes contenido DER del certificado
     * @return instancia de {@code X509Certificate}
     * @throws RuntimeException si el formato no es válido
     */
    private static X509Certificate toCertificate(byte[] certBytes) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
        } catch (CertificateException | java.security.NoSuchProviderException e) {
            throw new RuntimeException("No se pudo reconstruir el certificado X.509", e);
        }
    }

    /**
     * Convierte un {@code String} Base64 en un {@link X509Certificate}.
     *
     * @param base64Cert certificado codificado en Base64 (sin cabeceras PEM)
     * @return instancia de {@code X509Certificate}
     * @throws RuntimeException si el Base64 es inválido o no representa un certificado X.509
     */
    private static X509Certificate toCertificate(String base64Cert) {
        return toCertificate(decodeBase64(base64Cert));
    }
}
