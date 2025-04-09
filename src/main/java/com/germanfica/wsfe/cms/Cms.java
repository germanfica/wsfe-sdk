package com.germanfica.wsfe.cms;

import com.germanfica.wsfe.model.LoginTicketRequest;
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

public class Cms {
    private String signedCms;
    private final byte[] unsignedCms;

    private Cms(byte[] unsignedCms) {
        this.unsignedCms = unsignedCms;
        this.signedCms = null;
    }

    public static Cms createUnsigned(String keystorePath, String password, String signer, String dstDn, String service, Long ticketTime) {
        byte[] cmsData = CmsSigner.generateCms(keystorePath, password, signer, dstDn, service, ticketTime);
        return new Cms(cmsData);
    }

    public Cms sign() {
        if (this.signedCms != null) {
            throw new IllegalStateException("Este CMS ya está firmado.");
        }
        this.signedCms = CmsSigner.signCmsBase64(this.unsignedCms);
        return this;
    }

    public String getSignedValue() {
        if (this.signedCms == null) {
            throw new IllegalStateException("El CMS no ha sido firmado aún.");
        }
        return signedCms;
    }

    // Clase interna privada
    private static class CmsSigner {
        private static byte[] generateCms(String keystorePath, String password, String signer, String dstDn, String service, Long ticketTime) {
            PrivateKey privateKey = CryptoUtils.loadPrivateKey(keystorePath, password, signer);
            X509Certificate certificate = CryptoUtils.loadCertificate(keystorePath, password, signer);

            String loginTicketRequestXml = XMLUtils.createLoginTicketRequest(
                    certificate.getSubjectDN().toString(), dstDn, service, ticketTime);

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

        private static String signCmsBase64(byte[] unsignedCms) {
            return CryptoUtils.encodeBase64(unsignedCms);
        }
    }
}
