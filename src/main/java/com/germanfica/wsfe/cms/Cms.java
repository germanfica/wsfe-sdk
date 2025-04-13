package com.germanfica.wsfe.cms;

import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.util.CryptoUtils;
import com.germanfica.wsfe.util.XMLUtils;
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

    // Constructor privado para forzar el uso del método create()
    private Cms() {
    }

    /**
     * Crea un CMS firmado utilizando los parámetros provistos.
     *
     * @param params Instancia de CmsParams con los datos necesarios para generar el CMS.
     * @return Objeto Cms listo para ser utilizado.
     */
    public static Cms create(CmsParams params) {
        byte[] signedBytes = CmsSigner.sign(params);
        String base64 = CmsSigner.encodeBase64(signedBytes);

        Cms cms = new Cms();
        cms.signedCmsBase64 = base64;
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

            String loginTicketRequestXml = XMLUtils.createLoginTicketRequest(
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
}
