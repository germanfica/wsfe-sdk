package com.germanfica;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.model.LoginTicketData;
import com.germanfica.wsfe.model.LoginTicketResponseData;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.provider.ProviderChain;
import com.germanfica.wsfe.provider.cms.ApplicationPropertiesCmsParamsProvider;
import com.germanfica.wsfe.provider.cms.EnvironmentCmsParamsProvider;
import com.germanfica.wsfe.provider.cms.FileSignedCmsProvider;
import com.germanfica.wsfe.provider.cms.SystemPropertyCmsParamsProvider;
import com.germanfica.wsfe.provider.feauth.FileFEAuthParamsProvider;
import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.*;
import com.germanfica.wsfe.param.CmsParams;
import fev1.dif.afip.gov.ar.FERecuperaLastCbteResponse;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify if WSAA accepts a CMS whose TRA timestamps are old but within 24h.
 */
public class CmsValidityIntegrationTest {

    @Test
    void shouldRejectExpiredTra() throws Exception {
        // TRA vencido hace varios días
        String traXml =
            """
            <loginTicketRequest version="1.0">
              <header>
                <uniqueId>20002</uniqueId>
                <generationTime>2023-10-01T00:00:00.000-03:00</generationTime>
                <expirationTime>2024-10-02T00:00:00.000-03:00</expirationTime>
                <service>wsfe</service>
              </header>
            </loginTicketRequest>
            """;

        ProviderChain<CmsParams> providerChain = ProviderChain.<CmsParams>builder()
            .addProvider(new EnvironmentCmsParamsProvider())
            .addProvider(new SystemPropertyCmsParamsProvider())
            .addProvider(new ApplicationPropertiesCmsParamsProvider())
            .build();

        CmsParams cmsParams = providerChain.resolve()
            .orElseThrow(() -> new IllegalStateException("No se pudieron obtener los CmsParams."));
        Cms cms = Cms.create(cmsParams);

        FileSignedCmsProvider.save(cms.getSignedValue());
        System.out.println("💾 CMS guardado correctamente en ~/.wsfe/cms.ini");

        WsaaClient wsaa = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();

        // Se espera que ARCA rechace el TRA vencido con ApiException
        ApiException ex = assertThrows(ApiException.class, () -> {
            String xmlResponse = wsaa.authService().autenticar(cms);
            LoginTicketResponseData ta = (LoginTicketResponseData) LoginTicketParser.parse(xmlResponse);

            FEAuthParams feAuthParams = FEAuthParams.builder()
                .setToken(ta.token())
                .setSign(ta.sign())
                .setCuit(cms.getSubjectCuit())
                .setGenerationTime(ArcaDateTime.parse(ta.generationTime()))
                .setExpirationTime(ArcaDateTime.parse(ta.expirationTime()))
                .build();

            FileFEAuthParamsProvider.save(feAuthParams);
        }, "Debería lanzar ApiException porque el TRA está vencido o el TA sigue vigente.");

        System.out.println("⚠️ ApiException capturada: " + ex.getMessage());

        assertTrue(
            ex.getMessage().contains("vencido") ||
                ex.getMessage().contains("TA valido") ||
                ex.getMessage().contains("soap_fault"),
            "El mensaje de error debería indicar rechazo del TA/TRA."
        );

//        String xmlResponse = wsaa.authService().autenticar(cms);
//        System.out.println("XML Response:\n" + xmlResponse);
//
//        // Esperar error
//        assertTrue(xmlResponse.contains("<faultstring>") || xmlResponse.contains("<soap:Fault>"),
//            "WSAA debería rechazar el CMS porque el TRA está vencido.");
//
//        if (!xmlResponse.contains("<faultstring>")) {
//            fail("WSAA no devolvió error aunque el TRA está vencido.");
//        }
    }

    @Test
    void shouldRejectExpiredTraFromDisk() throws Exception {
        // (A) leer el ini en crudo (evitamos FileSignedCmsProvider.resolve() porque filtra TA vencidos)
        Path cmsFile = Paths.get(System.getProperty("user.home"), ".wsfe", "cms.ini");
        assertTrue(Files.exists(cmsFile), "Se esperaba encontrar ~/.wsfe/cms.ini para este test");

        SimpleIni ini = SimpleIni.load(cmsFile);
        String cmsBase64 = ini.get("default", "signedCms");
        assertNotNull(cmsBase64, "El ini debe contener la clave 'signedCms' en la seccion 'default'");

        // (B) construir el objeto Cms a partir del base64 guardado
        Cms cms = Cms.create(cmsBase64.trim());

        System.out.println("🧾 CMS leído desde disco (raw) length=" + cmsBase64.length());
        System.out.println(" - subject CUIT: " + maskCuit(cms.getSubjectCuit()));
        System.out.println(" - isTicketExpired: " + cms.isTicketExpired());

        LoginTicketData tra = CmsSignedExtractor.extractLoginTicket(CmsConverter.fromBase64(cmsBase64));

        System.out.println("TRA source: " + maskCuitInSource(tra.source()));
        System.out.println("TRA destination: " + tra.destination());
        System.out.println("TRA uniqueId: " + tra.uniqueId());
        System.out.println("TRA generation date: " + ArcaDateTime.parse(tra.generationTime()));
        System.out.println("TRA expiration date: " + ArcaDateTime.parse(tra.expirationTime()));
        System.out.println(" - isCertExpired: " + cms.isCertExpired());

        // (C) comprobamos que el TA efectivamente está vencido
        assertTrue(cms.isTicketExpired(), "Se esperaba que el TA embebido esté vencido para este test");

        // (D) crear un provider de prueba que devuelva EXACTAMENTE ese CMS (bypass del comportamiento de FileSignedCmsProvider)
        com.germanfica.wsfe.provider.CredentialsProvider<String> rawCmsProvider =
            new com.germanfica.wsfe.provider.CredentialsProvider<String>() {
                @Override
                public Optional<String> resolve() {
                    return Optional.of(cmsBase64.trim());
                }
            };

        // (E) intentar login con WSAA usando ese provider — debe fallar (ApiException)
        WsaaClient wsaa = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();
        String xmlResponse = wsaa.authService().autenticar(cms);

        if (xmlResponse.contains("<faultstring>") || xmlResponse.contains("<soap:Fault>")) {
            System.err.println("❌ WSAA returned an error response:");
            System.err.println(xmlResponse);
            fail("WSAA rejected the CMS. Check the SOAP fault above.");
        }
    }

    @Test
    void shouldRejectExpiredTa() throws Exception {
        // (1) Leer el TA guardado en ~/.wsfe/fe_auth.ini
        FileFEAuthParamsProvider feAuthProvider = new FileFEAuthParamsProvider();
        FEAuthParams authParams = feAuthProvider.resolve()
            .orElseThrow(() -> new IllegalStateException("No se encontró el TA guardado en disco (~/.wsfe/fe_auth.ini)"));

        System.out.println("🧾 TA leído desde disco:");
        System.out.println(" - CUIT: " + maskCuit(authParams.getCuit()));
        System.out.println(" - GenerationTime: " + authParams.getGenerationTime());
        System.out.println(" - ExpirationTime: " + authParams.getExpirationTime());

        // (2) Validar si el TA está vencido
        assertTrue(authParams.isExpired(), "El TA debería estar vencido para esta prueba.");

        // (3) Intentar usar el TA vencido en WSFE → debe fallar con ApiException
        WsfeClient wsfeClient = WsfeClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .setFEAuthParams(authParams)
            .build();

        FERecuperaLastCbteResponse response = wsfeClient.feCompUltimoAutorizado(1, 11);

        boolean hasErrors = WsfeResponseUtils.hasErrors(response.getErrors());
        int errorsCount = response.getErrors() != null && response.getErrors().getErr() != null
            ? response.getErrors().getErr().size()
            : 0;

        System.out.println("=== RAW WSFE RESPONSE ===");
        System.out.println("Has errors: " + hasErrors);
        System.out.println("Errors number: " + errorsCount);
        System.out.println("Errors: " + WsfeResponseUtils.joinErrors(response.getErrors()));
        System.out.println("Events: " + WsfeResponseUtils.joinEvents(response.getEvents()));
        System.out.printf("Último comprobante autorizado: %d%n", response.getCbteNro());
        System.out.println("=========================");

        assertTrue(errorsCount > 0, "Se esperaba al menos 1 error en la respuesta de WSFE.");

        boolean has600 = WsfeResponseUtils.anyErrorMatch(response.getErrors(),
            err -> err != null && err.getCode() == 600);
        assertTrue(has600, "Se esperaba encontrar al menos un error con code == 600 (ValidacionDeToken).");

        int cbteNro = response.getCbteNro();
        assertEquals(0, cbteNro, "Se esperaba cbteNro == null o 0 para TA no valido.");
    }

    @Test
    void shouldAcceptOldCmsIfWithin24Hours() throws Exception {
        // 1️⃣ Crear el TRA con generación "antigua" pero dentro de las 24h.
        // TRA generated on July 6, 2025 - nearly 3 months old.
//        String traXml =
//            """
//            <loginTicketRequest version="1.0">
//              <header>
//                <uniqueId>12345</uniqueId>
//                <generationTime>2025-07-06T02:00:00.000-03:00</generationTime>
//                <expirationTime>2025-07-06T03:00:00.000-03:00</expirationTime>
//                <service>wsfe</service>
//              </header>
//            </loginTicketRequest>
//            """;
        String traXml =
            """
            <loginTicketRequest version="1.0">
              <header>
                <uniqueId>12345</uniqueId>
                <generationTime>2025-07-06T02:00:00.000-03:00</generationTime>
                <expirationTime>2025-07-09T02:00:00.000-03:00</expirationTime>
                <service>wsfe</service>
              </header>
            </loginTicketRequest>
            """;

        // 2️⃣ Firmar ese TRA con el certificado válido (tu .p12)
//        CmsParams cmsParams = CmsParams.builder()
//            .setKeystorePath("C:/ruta/a/tu-certificado.p12")   // ruta real del .p12
//            .setPassword("clave")                              // contraseña real
//            .setSigner("cn=tuCertificado, o=TuEmpresa, c=AR")  // opcional
//            .setDstDn("cn=wsaahomo, o=afip, c=ar, serialnumber=CUIT 33693450239")
//            .setService("wsfe")
//            .setTicketTime(3600L) // no se usa, pero mantiene compatibilidad
//            .build();

        ProviderChain<CmsParams> providerChain = ProviderChain.<CmsParams>builder()
            .addProvider(new EnvironmentCmsParamsProvider())    // Usa variables de entorno
            .addProvider(new SystemPropertyCmsParamsProvider()) // Usa propiedades del sistema
            .addProvider(new ApplicationPropertiesCmsParamsProvider())
            .build();

        CmsParams cmsParams = providerChain.resolve()
            .orElseThrow(() -> new IllegalStateException("No se pudieron obtener los CmsParams."));

        Cms cms = Cms.create(cmsParams);

        // 3️⃣ Enviar el mismo CMS al WSAA
        WsaaClient wsaa = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();
        String xmlResponse = wsaa.authService().autenticar(cms);

        if (xmlResponse.contains("<faultstring>") || xmlResponse.contains("<soap:Fault>")) {
            System.err.println("❌ WSAA returned an error response:");
            System.err.println(xmlResponse);
            fail("WSAA rejected the CMS. Check the SOAP fault above.");
        }

        // 4️⃣ Parsear la respuesta
        LoginTicketResponseData ta = (LoginTicketResponseData) LoginTicketParser.parse(xmlResponse);

        // 5️⃣ Validar resultado
        System.out.println("TA recibido: " + ta.token());
        System.out.println("generationTime: " + ta.generationTime());
        System.out.println("expirationTime: " + ta.expirationTime());

        // validar TA

        //assertNotNull(data.token(), "WSAA devolvió un TA válido"); // NO ES SUFICIENTE PARA VALIDAR si es válido, los servidores de ARCA/AFIP determinan si es válido o no, ellos te pueden devolver un TA no válido, pero la manera de comprobarlo es haciendo una consulta con ese TA

        FEAuthParams feAuth = buildFeAuthParamsFromTa(ta, cms.getSubjectCuit());

        FERecuperaLastCbteResponse wsfeResp = callWsfeAndPrint(feAuth, 1, 11);

        validateTAResponse(wsfeResp);

        // Si esto pasa, el CMS fue aceptado aunque el TRA haya sido generado horas atrás.
    }

    // Construye FEAuthParams usando los datos del TA y el cuit
    private static FEAuthParams buildFeAuthParamsFromTa(LoginTicketResponseData ta, long cuit) {
        ArcaDateTime gen = ArcaDateTime.parse(ta.generationTime());
        ArcaDateTime exp = ArcaDateTime.parse(ta.expirationTime());

        return FEAuthParams.builder()
            .setToken(ta.token())
            .setSign(ta.sign())
            .setCuit(cuit)
            .setGenerationTime(gen)
            .setExpirationTime(exp)
            .build();
    }

    // Llama a WSFE con FEAuthParams, imprime respuesta/CAE/errores y devuelve el objeto Response
    private FERecuperaLastCbteResponse callWsfeAndPrint(FEAuthParams auth, int ptoVta, int cbteTipo) throws ApiException {
        System.out.println("Usando FEAuthParams: token=" + mask(auth.getToken()) + " cuit=" + auth.getCuit());

        WsfeClient feClient = WsfeClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .setFEAuthParams(auth)
            .build();

        FERecuperaLastCbteResponse response = feClient.feCompUltimoAutorizado(ptoVta, cbteTipo);

        System.out.println("=== WSFE: Último comprobante autorizado response ===");
        System.out.println("Raw response: " + response);

        // Mostrar errores si existen
        if (response != null && response.getErrors() != null && response.getErrors().getErr() != null
            && !response.getErrors().getErr().isEmpty()) {
            response.getErrors().getErr().forEach(err -> {
                System.out.println("Error " + err.getCode() + ": " + err.getMsg());
            });
        }

        return response;
    }

    // Valida la respuesta de WSFE: falla si hay errores o si cbteNro es null/0
    private static void validateTAResponse(FERecuperaLastCbteResponse response) {
        if (response == null) {
            fail("WSFE devolvió null en la respuesta.");
        }

        // Errores enviados por WSFE
        if (response.getErrors() != null && response.getErrors().getErr() != null
            && !response.getErrors().getErr().isEmpty()) {
            System.err.println("❌ WSFE returned errors:");
            response.getErrors().getErr().forEach(err -> {
                System.err.println("Error " + err.getCode() + ": " + err.getMsg());
            });
            fail("WSFE returned errors. See logs above.");
        }

        // Validar cbteNro: si es 0 o null considerarlo fallo
        Integer cbteNro = response.getCbteNro();
        if (cbteNro == null) {
            System.err.println("❌ WSFE response cbteNro is null.");
            fail("cbteNro is null. TA probably not valid for WSFE.");
        }
        if (cbteNro.intValue() == 0) {
            System.err.println("❌ WSFE response cbteNro == 0 (no comprobante válido).");
            fail("cbteNro == 0. TA probably not valid for WSFE.");
        }

        System.out.println("✅ WSFE returned cbteNro=" + cbteNro + " — TA funciona correctamente.");
    }

    private static String maskCuitInSource(String source) {
        if (source == null) return null;
        // Reemplaza cualquier número después de "CUIT " por asteriscos
        return source.replaceAll("(CUIT\\s+)\\d+", "$1***********");
    }


    /**
     * Enmascara el CUIT reemplazando cada carácter por '*'.
     */
    private String maskCuit(long cuit) {
        String cuitStr = String.valueOf(cuit);
        return "*".repeat(cuitStr.length());
    }

    private static String mask(String token) {
        if (token == null) return "null";
        if (token.length() <= 8) return token;
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
