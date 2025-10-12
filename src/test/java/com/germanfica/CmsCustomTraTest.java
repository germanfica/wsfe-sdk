package com.germanfica;

import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.util.CryptoUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;

/**
 * Unit test demonstrating how to build and sign a custom Login Ticket Request (TRA)
 * without modifying the {@link Cms} class.  The test programmatically generates
 * a temporary RSA key pair and a self‑signed X.509 certificate whose subject
 * contains a {@code SERIALNUMBER=CUIT} RDN.  A custom XML string representing
 * a loginTicketRequest is then signed using BouncyCastle to produce a CMS
 * (Cryptographic Message Syntax) envelope.  This signed CMS is passed to
 * {@link Cms#create(String)}, which extracts the CUIT from the certificate
 * and retains the original Base64 encoded value.  The assertions verify that
 * the custom CMS can be consumed by the SDK and that the CUIT was correctly
 * parsed from the certificate subject.
 */
public class CmsCustomTraTest {

    @Test
    @Tag("unit")
    @DisplayName("should create custom TRA and extract CUIT without contacting ARCA")
    void testCreateCustomTRA() throws Exception {
        // Ensure the BouncyCastle provider is registered.  CryptoUtils will
        // register it lazily, but we need it before generating keys and
        // certificates.  Registration is idempotent.
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        // ---------------------------------------------------------------------
        // 1) Generate an RSA key pair for signing
        // ---------------------------------------------------------------------
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // ---------------------------------------------------------------------
        // 2) Build a self‑signed X.509 certificate.  The subject must include
        //    a SERIALNUMBER RDN in the form "CUIT xxxxxxxxxxx" so that
        //    CmsCuitExtractor can parse the CUIT.  We choose 11 digits for
        //    the CUIT to resemble a valid value.  The certificate is valid
        //    from one day ago until one year in the future.
        // ---------------------------------------------------------------------
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "Test");
        nameBuilder.addRDN(BCStyle.O, "TestOrg");
        nameBuilder.addRDN(BCStyle.C, "AR");
        nameBuilder.addRDN(BCStyle.SERIALNUMBER, "CUIT 12345678901");
        X500Name subject = nameBuilder.build();

        Date notBefore = new Date(System.currentTimeMillis() - 24L * 60L * 60L * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24L * 60L * 60L * 1000L);
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

        ContentSigner certSigner = new JcaContentSignerBuilder("SHA256withRSA")
            .setProvider("BC")
            .build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
            subject,
            serialNumber,
            notBefore,
            notAfter,
            subject,
            keyPair.getPublic()
        );

        X509CertificateHolder certHolder = certBuilder.build(certSigner);
        X509Certificate certificate = new JcaX509CertificateConverter()
            .setProvider("BC")
            .getCertificate(certHolder);

        // ---------------------------------------------------------------------
        // 3) Compose a custom Login Ticket Request (TRA) XML.  We embed the
        //    certificate's subject as the source DN, set a destination DN,
        //    assign arbitrary values for uniqueId, generationTime and
        //    expirationTime, and specify the service name.  In a real
        //    environment these values would follow ARCA/WSAA guidelines.
        // ---------------------------------------------------------------------
        String subjectDnString = certificate.getSubjectX500Principal().getName();
        String destination = "CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239";
        String customXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<loginTicketRequest version=\"1.0\">" +
                "<header>" +
                "<source>" + subjectDnString + "</source>" +
                "<destination>" + destination + "</destination>" +
                "<uniqueId>999999999</uniqueId>" +
                // Static timestamps for reproducibility; adjust as needed
                "<generationTime>2025-01-01T00:00:00.000-03:00</generationTime>" +
                "<expirationTime>2025-12-31T23:59:59.000-03:00</expirationTime>" +
                "</header>" +
                "<service>wsfe</service>" +
                "</loginTicketRequest>";

        // ---------------------------------------------------------------------
        // 4) Sign the custom XML to create a CMS envelope.  This mirrors the
        //    logic found in CmsSigner.sign() but allows us to supply our own
        //    message (the TRA) without touching the Cms class.  The CMS
        //    encapsulates the data (encapsulate = true) so that receivers
        //    can extract the original TRA as part of the SignedData structure.
        // ---------------------------------------------------------------------
        ContentSigner cmsSigner = new JcaContentSignerBuilder("SHA1withRSA")
            .setProvider("BC")
            .build(keyPair.getPrivate());

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(
            new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
            ).build(cmsSigner, certHolder)
        );

        Store<X509CertificateHolder> certs = new JcaCertStore(Collections.singletonList(certHolder));
        generator.addCertificates(certs);

        CMSProcessableByteArray cmsData = new CMSProcessableByteArray(customXml.getBytes(StandardCharsets.UTF_8));
        CMSSignedData signedData = generator.generate(cmsData, true);
        byte[] encodedCms = signedData.getEncoded();
        String signedCmsBase64 = CryptoUtils.encodeBase64(encodedCms);

        // ---------------------------------------------------------------------
        // 5) Create a Cms object from our signed CMS.  This uses the SDK API
        //    without modifying the underlying Cms implementation.  The
        //    constructor will decode and re‑encode the CMS, extract the
        //    certificate and CUIT, and store the Base64 value for later use.
        // ---------------------------------------------------------------------
        Cms cms = Cms.create(signedCmsBase64);

        // ---------------------------------------------------------------------
        // 6) Assertions.  We verify that the CUIT embedded in our test
        //    certificate is correctly extracted and that the CMS is retained
        //    (non‑null).  Additional assertions can be added as needed.
        // ---------------------------------------------------------------------
        Assertions.assertNotNull(cms, "The Cms instance should not be null");
        Assertions.assertNotNull(cms.getSignedValue(), "The signed CMS value should not be null");
        Assertions.assertEquals(12345678901L, cms.getSubjectCuit(),
            "The CUIT extracted from the certificate should match the custom value");
    }
}