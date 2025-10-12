package com.germanfica;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.model.LoginTicketResponseData;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.provider.ProviderChain;
import com.germanfica.wsfe.provider.cms.ApplicationPropertiesCmsParamsProvider;
import com.germanfica.wsfe.provider.cms.EnvironmentCmsParamsProvider;
import com.germanfica.wsfe.provider.cms.SystemPropertyCmsParamsProvider;
import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.CryptoUtils;
import com.germanfica.wsfe.util.LoginTicketParser;
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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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

    private static final DateTimeFormatter TRA_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final String DEFAULT_DST_DN =
        "CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239";

    @Test
    @Tag("unit")
    @DisplayName("should create custom TRA and extract CUIT without contacting ARCA")
    void testCreateCustomTRA() throws Exception {
        // Ensure the BouncyCastle provider is registered.  CryptoUtils will
        // register it lazily, but we need it before generating keys and
        // certificates.  Registration is idempotent.
        ensureBouncyCastleProvider();

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
        String destination = DEFAULT_DST_DN;
        String customXml = createLoginTicketRequestXml(
            subjectDnString,
            destination,
            999_999_999L,
            "2025-01-01T00:00:00.000-03:00",
            "2025-12-31T23:59:59.000-03:00",
            "wsfe"
        );

        // ---------------------------------------------------------------------
        // 4) Sign the custom XML to create a CMS envelope.  This mirrors the
        //    logic found in CmsSigner.sign() but allows us to supply our own
        //    message (the TRA) without touching the Cms class.  The CMS
        //    encapsulates the data (encapsulate = true) so that receivers
        //    can extract the original TRA as part of the SignedData structure.
        // ---------------------------------------------------------------------
        String signedCmsBase64 = signTra(customXml, keyPair.getPrivate(), certificate);

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

    @Test
    @Tag("integration")
    @DisplayName("should send custom TRA to WSAA and receive a TA")
    void testSendCustomTRAUsingProviderChain() throws Exception {
        ensureBouncyCastleProvider();

        ProviderChain<CmsParams> providerChain = ProviderChain.<CmsParams>builder()
            .addProvider(new EnvironmentCmsParamsProvider())
            .addProvider(new SystemPropertyCmsParamsProvider())
            .addProvider(new ApplicationPropertiesCmsParamsProvider())
            .build();

        CmsParams cmsParams = providerChain.resolve().orElse(null);
        Assumptions.assumeTrue(cmsParams != null,
            "No se pudieron obtener los CmsParams. Configure variables de entorno o properties para ejecutar el test.");

        PrivateKey privateKey = CryptoUtils.loadPrivateKey(
            cmsParams.getKeystorePath(),
            cmsParams.getPassword(),
            cmsParams.getSigner()
        );
        X509Certificate certificate = CryptoUtils.loadCertificate(
            cmsParams.getKeystorePath(),
            cmsParams.getPassword(),
            cmsParams.getSigner()
        );

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("-03:00"));
        OffsetDateTime generationTime = now.minusMinutes(5);
        OffsetDateTime expirationTime = now.plusMinutes(30);

        String traXml = createLoginTicketRequestXml(
            certificate.getSubjectX500Principal().getName(),
            cmsParams.getDstDn() != null ? cmsParams.getDstDn() : DEFAULT_DST_DN,
            nextUniqueId(),
            formatTraTime(generationTime),
            formatTraTime(expirationTime),
            cmsParams.getService()
        );

        String signedCmsBase64 = signTra(traXml, privateKey, certificate);
        Cms cms = Cms.create(signedCmsBase64);

        WsaaClient wsaa = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();

        String xmlResponse = wsaa.authService().autenticar(cms);
        System.out.println("WSAA response: " + xmlResponse);

        Assertions.assertFalse(xmlResponse.contains("<faultstring>"),
            () -> "WSAA devolvió un faultstring: " + xmlResponse);
        Assertions.assertFalse(xmlResponse.contains("<soap:Fault>"),
            () -> "WSAA devolvió un SOAP Fault: " + xmlResponse);

        LoginTicketResponseData ta = (LoginTicketResponseData) LoginTicketParser.parse(xmlResponse);

        Assertions.assertNotNull(ta, "El WSAA debe devolver un TA");
        Assertions.assertNotNull(ta.token(), "El TA debe contener un token");
        Assertions.assertNotNull(ta.sign(), "El TA debe contener una firma");

        FEAuthParams feAuthParams = buildFeAuthParams(ta, cms.getSubjectCuit());
        Assertions.assertEquals(ta.token(), feAuthParams.getToken());
        Assertions.assertEquals(ta.sign(), feAuthParams.getSign());
        Assertions.assertEquals(cms.getSubjectCuit(), feAuthParams.getCuit());
    }

    private static void ensureBouncyCastleProvider() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private static String createLoginTicketRequestXml(
        String source,
        String destination,
        long uniqueId,
        String generationTime,
        String expirationTime,
        String service
    ) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<loginTicketRequest version=\"1.0\">"
            + "<header>"
            + "<source>" + source + "</source>"
            + "<destination>" + destination + "</destination>"
            + "<uniqueId>" + uniqueId + "</uniqueId>"
            + "<generationTime>" + generationTime + "</generationTime>"
            + "<expirationTime>" + expirationTime + "</expirationTime>"
            + "</header>"
            + "<service>" + service + "</service>"
            + "</loginTicketRequest>";
    }

    private static String signTra(String traXml, PrivateKey privateKey, X509Certificate certificate) throws Exception {
        ContentSigner cmsSigner = new JcaContentSignerBuilder("SHA1withRSA")
            .setProvider("BC")
            .build(privateKey);

        X509CertificateHolder certHolder = new X509CertificateHolder(certificate.getEncoded());

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(
            new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
            ).build(cmsSigner, certHolder)
        );

        Store<X509CertificateHolder> certs = new JcaCertStore(Collections.singletonList(certHolder));
        generator.addCertificates(certs);

        CMSProcessableByteArray cmsData = new CMSProcessableByteArray(traXml.getBytes(StandardCharsets.UTF_8));
        CMSSignedData signedData = generator.generate(cmsData, true);
        return CryptoUtils.encodeBase64(signedData.getEncoded());
    }

    private static String formatTraTime(OffsetDateTime time) {
        return TRA_TIME_FORMATTER.format(time);
    }

    private static long nextUniqueId() {
        return System.currentTimeMillis() / 1000L;
    }

    private static FEAuthParams buildFeAuthParams(LoginTicketResponseData ta, long cuit) {
        return FEAuthParams.builder()
            .setToken(ta.token())
            .setSign(ta.sign())
            .setCuit(cuit)
            .setGenerationTime(ArcaDateTime.parse(ta.generationTime()))
            .setExpirationTime(ArcaDateTime.parse(ta.expirationTime()))
            .build();
    }
}