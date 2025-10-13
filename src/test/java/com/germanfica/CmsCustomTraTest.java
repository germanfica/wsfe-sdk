package com.germanfica;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.exception.ApiException;
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
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.cms.SignerInfo;
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
import org.junit.jupiter.api.function.Executable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

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

    @Test
    @Tag("integration")
    @DisplayName("CEE no autorizado a acceder a los servicios de ARCA. No deberá solicitar nuevos TA hasta gestionar el acceso WSN correspondiente.")
    void shouldReturnCoeNotAuthorized() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> {
            builder.withService("wsmtxca");
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "coe.notAuthorized");
    }

    @Test
    @Tag("integration")
    @DisplayName("El CEE ha solicitado un ticket de acceso para el cual ya dispone de TA válidos. No deberá solicitar nuevos TA mientras disponga de uno válido.")
    void shouldReturnCoeAlreadyAuthenticated() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> { });

        try {
            ctx.wsaa.authService().autenticar(signedCms);
        } catch (ApiException e) {
            Assumptions.assumeTrue(false, "No se pudo obtener un TA inicial: " + e.getMessage());
        }

        ApiException fault = Assertions.assertThrows(ApiException.class,
            () -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "coe.alreadyAuthenticated");
    }

    @Test
    @Tag("integration")
    @DisplayName("El CMS no es válido.")
    void shouldReturnCmsBad() throws Exception {
        WsaaClient wsaa = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();

        String invalidCms = Base64.getEncoder().encodeToString("invalid-cms".getBytes(StandardCharsets.UTF_8));
        ApiException fault = expectSoapFault(() -> wsaa.authService().autenticar(invalidCms));
        // código esperado
        assertFaultContains(fault, "cms.bad");
    }

    @Test
    @Tag("integration")
    @DisplayName("No se puede decodificar el BASE64.")
    void shouldReturnCmsBadBase64() throws Exception {
        WsaaClient wsaa = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();

        ApiException fault = expectSoapFault(() -> wsaa.authService().autenticar("%%%"));
        // código esperado
        assertFaultContains(fault, "cms.bad.base64");
    }

    @Test
    @Tag("integration")
    @DisplayName("No se ha encontrado certificado de firma en el CMS.")
    void shouldReturnCmsCertNotFound() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTraWithoutCertificate();

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "cms.cert.notFound");
    }

    @Test
    @Tag("integration")
    @DisplayName("Firma inválida o algoritmo no soportado.")
    void shouldReturnCmsSignInvalid() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> { });

        String tamperedCms = tamperCmsSignature(signedCms);

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(tamperedCms));
        // código esperado
        assertFaultContains(fault, "cms.sign.invalid");
    }

    private static String tamperCmsSignature(String signedCms) throws Exception {
        byte[] cmsBytes = Base64.getDecoder().decode(signedCms);

        ContentInfo contentInfo;
        try (ASN1InputStream asn1InputStream = new ASN1InputStream(cmsBytes)) {
            ASN1Primitive asn1Primitive = asn1InputStream.readObject();
            contentInfo = ContentInfo.getInstance(asn1Primitive);
        }

        SignedData signedData = SignedData.getInstance(contentInfo.getContent());
        ASN1Set signerInfos = signedData.getSignerInfos();

        ASN1Encodable[] tamperedSignerInfos = new ASN1Encodable[signerInfos.size()];
        for (int i = 0; i < signerInfos.size(); i++) {
            SignerInfo signerInfo = SignerInfo.getInstance(signerInfos.getObjectAt(i));
            byte[] signature = signerInfo.getEncryptedDigest().getOctets();
            byte[] tamperedSignature = signature.clone();
            tamperedSignature[0] ^= 0x01;

            DEROctetString tamperedDigest = new DEROctetString(tamperedSignature);
            tamperedSignerInfos[i] = new SignerInfo(
                signerInfo.getSID(),
                signerInfo.getDigestAlgorithm(),
                signerInfo.getAuthenticatedAttributes(),
                signerInfo.getDigestEncryptionAlgorithm(),
                tamperedDigest,
                signerInfo.getUnauthenticatedAttributes()
            );
        }

        SignedData tamperedSignedData = new SignedData(
            signedData.getDigestAlgorithms(),
            signedData.getEncapContentInfo(),
            signedData.getCertificates(),
            signedData.getCRLs(),
            new DERSet(tamperedSignerInfos)
        );

        ContentInfo tamperedContentInfo = new ContentInfo(
            contentInfo.getContentType(),
            tamperedSignedData
        );

        return Base64.getEncoder().encodeToString(tamperedContentInfo.getEncoded());
    }

    @Test
    @Tag("integration")
    @DisplayName("Certificado expirado.")
    void shouldReturnCmsCertExpired() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        GeneratedCertificate certificate = GeneratedCertificate.expired();
        String signedCms = ctx.signWithCustomCertificate(certificate, builder -> { });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "cms.cert.expired");
    }

    @Test
    @Tag("integration")
    @DisplayName("Certificado con fecha de generación posterior a la actual.")
    void shouldReturnCmsCertInvalid() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        GeneratedCertificate certificate = GeneratedCertificate.notYetValid();
        String signedCms = ctx.signWithCustomCertificate(certificate, builder -> { });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "cms.cert.invalid");
    }

    @Test
    @Tag("integration")
    @DisplayName("Certificado no emitido por una AC de confianza.")
    void shouldReturnCmsCertUntrusted() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        GeneratedCertificate certificate = GeneratedCertificate.untrusted();
        String signedCms = ctx.signWithCustomCertificate(certificate, builder -> { });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "cms.cert.untrusted");
    }

    @Test
    @Tag("integration")
    @DisplayName("No se ha podido interpretar el XML contra el schema.")
    void shouldReturnXmlBad() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> {
            builder.withCustomXml("<loginTicketRequest>");
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "xml.bad");
    }

    @Test
    @Tag("integration")
    @DisplayName("El atributo `source` no se corresponde con el DN del certificado.")
    void shouldReturnXmlSourceInvalid() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> {
            builder.withSource("CN=otro, SERIALNUMBER=CUIT 20111111112");
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "xml.source.invalid");
    }

    @Test
    @Tag("integration")
    @DisplayName("El atributo `destination` no se corresponde con el DN del WSAA.")
    void shouldReturnXmlDestinationInvalid() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> {
            builder.withDestination("CN=invalid");
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "xml.destination.invalid");
    }

    @Test
    @Tag("integration")
    @DisplayName("La versión del documento no es soportada.")
    void shouldReturnXmlVersionNotSupported() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> {
            builder.withVersion("2.0");
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "xml.version.notSupported");
    }

    @Test
    @Tag("integration")
    @DisplayName("El tiempo de generación es posterior a la hora actual o tiene más de 24 horas de antigüedad.")
    void shouldReturnXmlGenerationTimeInvalid() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("-03:00"));
        String signedCms = ctx.signTra(builder -> {
            builder.withGenerationTime(formatTraTime(now.plusMinutes(10)));
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "xml.generationTime.invalid");
    }

    @Test
    @Tag("integration")
    @DisplayName("El tiempo de expiración es inferior a la hora actual.")
    void shouldReturnXmlExpirationTimeExpired() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("-03:00"));
        String signedCms = ctx.signTra(builder -> {
            builder.withExpirationTime(formatTraTime(now.minusMinutes(1)));
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "xml.expirationTime.expired");
    }

    @Test
    @Tag("integration")
    @DisplayName("El tiempo de expiración del documento es superior a 24 horas.")
    void shouldReturnXmlExpirationTimeInvalid() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("-03:00"));
        String signedCms = ctx.signTra(builder -> {
            builder.withExpirationTime(formatTraTime(now.plusHours(30)));
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "xml.expirationTime.invalid");
    }

    @Test
    @Tag("integration")
    @DisplayName("El servicio al que se desea acceder se encuentra momentáneamente fuera de servicio.")
    void shouldReturnWsnUnavailable() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> {
            builder.withService("wsfe");
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "wsn.unavailable");
    }

    @Test
    @Tag("integration")
    @DisplayName("Servicio informado inexistente.")
    void shouldReturnWsnNotFound() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> {
            builder.withService("servicio_inexistente");
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "wsn.notFound");
    }

    @Test
    @Tag("integration")
    @DisplayName("El servicio de autenticación/autorización se encuentra momentáneamente fuera de servicio.")
    void shouldReturnWsaaUnavailable() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepareWithCustomEndpoint("https://wsaahomo.afip.gov.ar:4444");
        ApiException fault = Assertions.assertThrows(ApiException.class,
            () -> ctx.wsaa.authService().autenticar(ctx.signTra(builder -> { })));
        // código esperado
        assertFaultContains(fault, "wsaa.unavailable");
    }

    @Test
    @Tag("integration")
    @DisplayName("No se ha podido procesar el requerimiento.")
    void shouldReturnWsaaInternalError() throws Exception {
        IntegrationContext ctx = IntegrationContext.prepare();
        String signedCms = ctx.signTra(builder -> {
            builder.withCustomXml("<loginTicketRequest version=\"1.0\"><header/></loginTicketRequest>");
        });

        ApiException fault = expectSoapFault(() -> ctx.wsaa.authService().autenticar(signedCms));
        // código esperado
        assertFaultContains(fault, "wsaa.internalError");
    }

    private static ApiException expectSoapFault(Executable executable) {
        return Assertions.assertThrows(ApiException.class, executable);
    }

    private static void assertFaultContains(ApiException fault, String expectedCode) {
        String faultCode = fault.getErrorDto() != null ? fault.getErrorDto().getFaultCode() : null;
        Assertions.assertTrue(
            faultCode != null && fault.getErrorDto().getFaultCode().contains(expectedCode),
            () -> "Se esperaba código " + expectedCode + " pero se obtuvo: " + fault.getErrorDto().getFaultCode()
        );
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
        return createLoginTicketRequestXml(
            source,
            destination,
            uniqueId,
            generationTime,
            expirationTime,
            service,
            "1.0"
        );
    }

    private static String createLoginTicketRequestXml(
        String source,
        String destination,
        long uniqueId,
        String generationTime,
        String expirationTime,
        String service,
        String version
    ) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<loginTicketRequest version=\"" + version + "\">"
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

    private static String signTraWithoutCertificate(String traXml, PrivateKey privateKey, X509Certificate certificate) throws Exception {
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

    private static final class IntegrationContext {
        private final WsaaClient wsaa;
        private final CmsParams cmsParams;
        private final PrivateKey privateKey;
        private final X509Certificate certificate;

        private IntegrationContext(
            WsaaClient wsaa,
            CmsParams cmsParams,
            PrivateKey privateKey,
            X509Certificate certificate
        ) {
            this.wsaa = wsaa;
            this.cmsParams = cmsParams;
            this.privateKey = privateKey;
            this.certificate = certificate;
        }

        private static IntegrationContext prepare() throws Exception {
            ensureBouncyCastleProvider();

            ProviderChain<CmsParams> providerChain = ProviderChain.<CmsParams>builder()
                .addProvider(new EnvironmentCmsParamsProvider())
                .addProvider(new SystemPropertyCmsParamsProvider())
                .addProvider(new ApplicationPropertiesCmsParamsProvider())
                .build();

            Optional<CmsParams> optionalParams = providerChain.resolve();
            Assumptions.assumeTrue(optionalParams.isPresent(),
                "No se pudieron obtener los CmsParams. Configure variables de entorno o properties para ejecutar el test.");

            CmsParams cmsParams = optionalParams.get();
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

            WsaaClient wsaa = WsaaClient.builder()
                .setApiEnvironment(ApiEnvironment.HOMO)
                .build();

            return new IntegrationContext(wsaa, cmsParams, privateKey, certificate);
        }

        private static IntegrationContext prepareWithCustomEndpoint(String urlBase) throws Exception {
            IntegrationContext base = prepare();
            WsaaClient wsaa = WsaaClient.builder()
                .setUrlBase(urlBase)
                .setApiEnvironment(ApiEnvironment.HOMO)
                .build();
            return new IntegrationContext(wsaa, base.cmsParams, base.privateKey, base.certificate);
        }

        private String signTra(Consumer<TraBuilder> customizer) throws Exception {
            TraBuilder builder = TraBuilder.defaultBuilder(this);
            if (customizer != null) {
                customizer.accept(builder);
            }
            return CmsCustomTraTest.signTra(
                builder.buildXml(),
                builder.resolvePrivateKey(this),
                builder.resolveCertificate(this)
            );
        }

        private String signTraWithoutCertificate() throws Exception {
            TraBuilder builder = TraBuilder.defaultBuilder(this);
            return CmsCustomTraTest.signTraWithoutCertificate(
                builder.buildXml(),
                this.privateKey,
                this.certificate
            );
        }

        private String signWithCustomCertificate(GeneratedCertificate certificate, Consumer<TraBuilder> customizer) throws Exception {
            TraBuilder builder = TraBuilder.defaultBuilder(this)
                .withCertificate(certificate.certificate, certificate.privateKey)
                .withSource(certificate.certificate.getSubjectX500Principal().getName());
            if (customizer != null) {
                customizer.accept(builder);
            }
            return CmsCustomTraTest.signTra(
                builder.buildXml(),
                certificate.privateKey,
                certificate.certificate
            );
        }
    }

    private static final class TraBuilder {
        private String source;
        private String destination;
        private long uniqueId;
        private String generationTime;
        private String expirationTime;
        private String service;
        private String version = "1.0";
        private String customXml;
        private PrivateKey overridePrivateKey;
        private X509Certificate overrideCertificate;

        private static TraBuilder defaultBuilder(IntegrationContext ctx) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("-03:00"));
            TraBuilder builder = new TraBuilder();
            builder.source = ctx.certificate.getSubjectX500Principal().getName();
            builder.destination = ctx.cmsParams.getDstDn() != null ? ctx.cmsParams.getDstDn() : DEFAULT_DST_DN;
            builder.uniqueId = nextUniqueId();
            builder.generationTime = formatTraTime(now.minusMinutes(5));
            builder.expirationTime = formatTraTime(now.plusMinutes(30));
            builder.service = ctx.cmsParams.getService();
            return builder;
        }

        private TraBuilder withService(String service) {
            this.service = service;
            return this;
        }

        private TraBuilder withSource(String source) {
            this.source = source;
            return this;
        }

        private TraBuilder withDestination(String destination) {
            this.destination = destination;
            return this;
        }

        private TraBuilder withGenerationTime(String generationTime) {
            this.generationTime = generationTime;
            return this;
        }

        private TraBuilder withExpirationTime(String expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        private TraBuilder withVersion(String version) {
            this.version = version;
            return this;
        }

        private TraBuilder withCustomXml(String customXml) {
            this.customXml = customXml;
            return this;
        }

        private TraBuilder withCertificate(X509Certificate certificate, PrivateKey privateKey) {
            this.overrideCertificate = certificate;
            this.overridePrivateKey = privateKey;
            return this;
        }

        private String buildXml() {
            if (customXml != null) {
                return customXml;
            }
            return createLoginTicketRequestXml(
                source,
                destination,
                uniqueId,
                generationTime,
                expirationTime,
                service,
                version
            );
        }

        private PrivateKey resolvePrivateKey(IntegrationContext ctx) {
            return overridePrivateKey != null ? overridePrivateKey : ctx.privateKey;
        }

        private X509Certificate resolveCertificate(IntegrationContext ctx) {
            return overrideCertificate != null ? overrideCertificate : ctx.certificate;
        }
    }

    private static final class GeneratedCertificate {
        private final PrivateKey privateKey;
        private final X509Certificate certificate;

        private GeneratedCertificate(PrivateKey privateKey, X509Certificate certificate) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }

        private static GeneratedCertificate expired() throws Exception {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            Date notBefore = Date.from(now.minusDays(30).toInstant());
            Date notAfter = Date.from(now.minusDays(1).toInstant());
            return generateSelfSigned(notBefore, notAfter);
        }

        private static GeneratedCertificate notYetValid() throws Exception {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            Date notBefore = Date.from(now.plusDays(1).toInstant());
            Date notAfter = Date.from(now.plusDays(30).toInstant());
            return generateSelfSigned(notBefore, notAfter);
        }

        private static GeneratedCertificate untrusted() throws Exception {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            Date notBefore = Date.from(now.minusDays(1).toInstant());
            Date notAfter = Date.from(now.plusDays(30).toInstant());
            return generateSelfSigned(notBefore, notAfter);
        }

        private static GeneratedCertificate generateSelfSigned(Date notBefore, Date notAfter) throws Exception {
            ensureBouncyCastleProvider();

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
            nameBuilder.addRDN(BCStyle.CN, "Test");
            nameBuilder.addRDN(BCStyle.O, "TestOrg");
            nameBuilder.addRDN(BCStyle.C, "AR");
            nameBuilder.addRDN(BCStyle.SERIALNUMBER, "CUIT 20987654321");
            X500Name subject = nameBuilder.build();

            ContentSigner certSigner = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());

            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject,
                BigInteger.valueOf(System.nanoTime()),
                notBefore,
                notAfter,
                subject,
                keyPair.getPublic()
            );

            X509CertificateHolder certHolder = certBuilder.build(certSigner);
            X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

            return new GeneratedCertificate(keyPair.getPrivate(), certificate);
        }
    }
}