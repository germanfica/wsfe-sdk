package com.germanfica.wsfe.cms;

import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.util.CmsFormatInspector;
import com.germanfica.wsfe.util.CryptoUtils;
import com.germanfica.wsfe.util.X500Utils;
import com.germanfica.wsfe.util.XmlUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;

// TODO (#4): Evaluar si se debe abstraer la firma CMS dentro del SDK o permitir que el usuario la genere externamente.
// Ver discusión y propuesta completa en: https://github.com/germanfica/wsfe-sdk/issues/4
/**
 * Representa un CMS firmado que puede utilizarse para autenticarse con WSAA (AFIP/ARCA).
 * La lógica de firmado se encapsula dentro de la clase y se construye usando un factory method.
 */
public class Cms {
    private String signedCmsBase64;
    private long subjectCuit;

    // Constructor privado para forzar el uso del método create()
    private Cms() {
    }

    // -------------------------------------------------------------------------
    //  NUEVO  factory method: crea un Cms desde un CMS ya firmado (Base64)
    // -------------------------------------------------------------------------
    /**
     * Construye una instancia {@code Cms} a partir de un CMS ya firmado
     * (codificado en Base64).  Útil para CLIs, micro-servicios o apps de
     * escritorio donde el CMS se genera fuera del runtime.
     *
     * @param signedCmsBase64 CMS firmado en Base64 (“cmsFirmado”)
     * @return objeto {@code Cms} listo para usarse con WSAA/WSFE
     * @throws IllegalArgumentException si la cadena es nula/vacía o no es un CMS
     */
    public static Cms create(String signedCmsBase64) {
        if (signedCmsBase64 == null || signedCmsBase64.isBlank()) {
            throw new IllegalArgumentException("signedCmsBase64 vacío o nulo");
        }
        if (!CmsFormatInspector.isCmsSigned(signedCmsBase64)) {
            throw new IllegalArgumentException("El valor provisto no es un CMS válido");
        }
        return create(CryptoUtils.decodeBase64(signedCmsBase64.trim()));
    }

    /**
     * Crea un CMS firmado utilizando los parámetros provistos.
     *
     * @param params Instancia de CmsParams con los datos necesarios para generar el CMS.
     * @return Objeto Cms listo para ser utilizado.
     */
    public static Cms create(CmsParams params) {
        return create(CmsSigner.sign(params));
    }

    // -------------------------------------------------------------------------
    //  entry point
    // -------------------------------------------------------------------------
    private static Cms create(byte[] cmsBytes) {
        // Build the domain object once, here
        Cms cms                = new Cms();
        cms.signedCmsBase64    = CryptoUtils.encodeBase64(cmsBytes);
        cms.subjectCuit        = CmsCuitExtractor.extractSubjectCuit(cmsBytes);
        return cms;
    }

    /**
     * Devuelve el valor firmado del CMS en formato Base64.
     *
     * @return cadena Base64 del CMS firmado.
     * @throws IllegalStateException si el CMS no fue firmado correctamente.
     */
    public String getSignedValue() {
        if (this.signedCmsBase64 == null) {
            throw new IllegalStateException("El CMS no ha sido firmado aún.");
        }
        return signedCmsBase64;
    }

    /**
     * Devuelve el CUIT del titular del certificado, también conocido como <i>subject</i>.
     *
     * <p>Este CUIT se extrae del campo {@code SERIALNUMBER} del Distinguished Name (DN)
     * del certificado, donde se espera el formato {@code "CUIT 30XXXXXXXXX"} según lo definido
     * por ARCA/AFIP.</p>
     *
     * @return CUIT del sujeto del certificado digital (titular)
     * @throws IllegalStateException si el CMS no fue firmado aún
     */
    public long getSubjectCuit() {
        if (this.signedCmsBase64 == null) {
            throw new IllegalStateException("El CMS no ha sido firmado aún.");
        }
        return subjectCuit;
    }

    /**
     * Clase interna encargada de realizar la firma digital utilizando BouncyCastle.
     */
    private static final class CmsSigner {
        private static byte[] sign(CmsParams params) {
            PrivateKey privateKey = CryptoUtils.loadPrivateKey(
                    params.getKeystorePath(), params.getPassword(), params.getSigner()
            );
            X509Certificate certificate = CryptoUtils.loadCertificate(
                    params.getKeystorePath(), params.getPassword(), params.getSigner()
            );

            String loginTicketRequestXml = XmlUtils.createLoginTicketRequest(
                    certificate.getSubjectDN().toString(),
                    params.getDstDn(),
                    params.getService(),
                    params.getTicketTime()
            );

            //System.out.println(loginTicketRequestXml);
            //System.out.println("SIGNER: " + signer);
            //System.out.println("SIGNER DN: " + certificate.getSubjectDN().toString());

            try {
                ContentSigner contentSigner = new JcaContentSignerBuilder("SHA1withRSA")
                        .setProvider("BC")
                        .build(privateKey);

                CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
                gen.addSignerInfoGenerator(
                        new JcaSignerInfoGeneratorBuilder(
                                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
                        ).build(contentSigner, new X509CertificateHolder(certificate.getEncoded()))
                );

                Store certStore = new JcaCertStore(Collections.singletonList(certificate));
                gen.addCertificates(certStore);

                CMSProcessableByteArray data = new CMSProcessableByteArray(loginTicketRequestXml.getBytes());
                CMSSignedData signedData = gen.generate(data, true);

                return signedData.getEncoded();

            } catch (Exception e) {
                throw new RuntimeException("Error al firmar CMS", e);
            }
        }

        private static String encodeBase64(byte[] signedCms) {
            return CryptoUtils.encodeBase64(signedCms);
        }
    }
    private static final class CmsCuitExtractor {
        private CmsCuitExtractor() {}  // utility class

        static long extractSubjectCuit(byte[] cmsBytes) {
            X509Certificate certificate = CryptoUtils.extractCertificate(cmsBytes);
            return extractSubjectCuit(certificate.getSubjectX500Principal());
        }

        static long extractSubjectCuit(String cmsBase64) {
            return extractSubjectCuit(CryptoUtils.decodeBase64(cmsBase64));
        }

        /**
         * Extract the CUIT from the <i>SERIALNUMBER</i> RDN within the given principal.
         * Expected AFIP formatting: {@code "CUIT 30711222334"}.
         *
         * @param principal X.500 principal to inspect
         * @return CUIT as {@code long}
         * @throws IllegalStateException if the CUIT cannot be found or parsed
         */
        public static long extractSubjectCuit(X500Principal principal) {
            try {
                LdapName dn = new LdapName(principal.getName(X500Principal.RFC2253));
                for (Rdn rdn : dn.getRdns()) {
                    String type = X500Utils.normalizeType(rdn.getType());
                    if ("SERIALNUMBER".equalsIgnoreCase(type)) {
                        String value = X500Utils.decodeRdnValue(rdn.getValue());
                        String[] parts = value.split("\\s+");
                        if (parts.length == 2 && "CUIT".equalsIgnoreCase(parts[0])) {
                            return Long.parseLong(parts[1]);
                        }
                    }
                }
                throw new IllegalStateException("CUIT (SERIALNUMBER) not found in DN: " + dn);
            } catch (InvalidNameException e) {
                throw new RuntimeException("Invalid DN: " + principal, e);
            }
        }
    }
}
