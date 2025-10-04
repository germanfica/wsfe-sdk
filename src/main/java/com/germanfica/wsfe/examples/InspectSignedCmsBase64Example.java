package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.util.CmsSignedExtractor;
import com.germanfica.wsfe.util.CryptoUtils;
import org.bouncycastle.cms.CMSSignedData;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Properties;

public class InspectSignedCmsBase64Example {
    private static final Properties properties = new Properties();
    public static void main(String[] args) {
        try {
            try (FileInputStream input = new FileInputStream("src/main/resources/application.properties")) {
                properties.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Error al cargar el archivo de configuración", e);
            }

            // 1) Armar CMS para WSAA
            String signedCmsBase64 = properties.getProperty("wsaa.cms.signed-cms-base64");
            CMSSignedData cmsSignedData = new CMSSignedData(CryptoUtils.decodeBase64(signedCmsBase64));

            System.out.println("=== CMS Inspection ===");
            System.out.println("ticket signingTime:     " + CmsSignedExtractor.extractSigningTime(cmsSignedData));
            System.out.println("ticket generationTime:  " + CmsSignedExtractor.extractTicketGenerationTime(cmsSignedData));
            System.out.println("ticket expirationTime:  " + CmsSignedExtractor.extractTicketExpirationTime(cmsSignedData));
            System.out.println("certificate valid from: " + CmsSignedExtractor.extractCertificateValidFrom(cmsSignedData));
            System.out.println("certificate valid to:   " + CmsSignedExtractor.extractCertificateValidTo(cmsSignedData));
            System.out.println("ticket details:         " + CmsSignedExtractor.extractTicketDetails(cmsSignedData));
            System.out.println();

            // Raw XML
            System.out.println("=== Raw LoginTicket XML ===");
            System.out.println(CmsSignedExtractor.extractRawXml(cmsSignedData));
            System.out.println();

            // Certificados
            System.out.println("=== Certificates in CMS ===");
            for (X509Certificate cert : CmsSignedExtractor.extractAllCertificates(cmsSignedData)) {
                System.out.println("Subject: " + cert.getSubjectDN());
                System.out.println("Issuer:  " + cert.getIssuerDN());
                System.out.println("Serial:  " + cert.getSerialNumber());
                System.out.println();
            }

            // Signers
            System.out.println("=== Signer Info ===");
            CmsSignedExtractor.extractSignerInfo(cmsSignedData).forEach(System.out::println);
            System.out.println();

            // Signed attributes
            System.out.println("=== Signed Attributes ===");
            CmsSignedExtractor.extractSignedAttributes(cmsSignedData).forEach(System.out::println);
            System.out.println();

            // Signature verification
            System.out.println("=== Signature Verification ===");
            System.out.println("Signature verified: " + CmsSignedExtractor.verifySignature(cmsSignedData));

        } catch (Exception e) {
            System.err.println("Error desconocido: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
