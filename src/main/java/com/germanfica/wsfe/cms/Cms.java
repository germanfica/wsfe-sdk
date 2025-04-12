package com.germanfica.wsfe.cms;

import com.germanfica.wsfe.utils.CryptoUtils;
import com.germanfica.wsfe.utils.XMLUtils;
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
public class Cms {
    private final String signedCmsBase64;

    public Cms(String keystorePath, String password, String signer, String dstDn, String service, Long ticketTime) {
        byte[] signCms = CmsSigner.sign(keystorePath, password, signer, dstDn, service, ticketTime);
        this.signedCmsBase64 = CmsSigner.encodeBase64(signCms);
    }

    public String getSignedValue() {
        if (this.signedCmsBase64 == null) {
            throw new IllegalStateException("El CMS no ha sido firmado aún.");
        }
        return signedCmsBase64;
    }

    // Clase interna privada
    private static final class CmsSigner {
        private static byte[] sign(String keystorePath, String password, String signer, String dstDn, String service, Long ticketTime) {
            PrivateKey privateKey = CryptoUtils.loadPrivateKey(keystorePath, password, signer);
            X509Certificate certificate = CryptoUtils.loadCertificate(keystorePath, password, signer);

            String loginTicketRequestXml = XMLUtils.createLoginTicketRequest(
                    certificate.getSubjectDN().toString(), dstDn, service, ticketTime);

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
