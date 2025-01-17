package com.germanfica.wsfe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.dto.LoginCmsResponseDto;
import com.germanfica.wsfe.utils.ArcaWSAAUtils;
import generated.LoginTicketResponseType;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ArcaWSAAClientService {
    private final String endpoint = Wsfe.getWsaaBase() + "/ws/services/LoginCms";
    @Value("${keystore}")
    private String keystorePath;

    @Value("${keystore-password}")
    private String keystorePassword;

    @Value("${keystore-signer}")
    private String keystoreSigner;

    @Value("${dstdn:cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239}")
    private String dstdn;

    @Value("${TicketTime:36000}")
    private Long ticketTime;

    @Value("${service}")
    private String service;

    // == constructors ==

    private ArcaWSAAClientService(){ }

    // == methods ==
    public LoginCmsResponseDto invokeWsaa() {
        try {
            // Crear el CMS
            byte[] loginTicketRequestXmlCms = ArcaWSAAUtils.create_cms(
                    keystorePath,
                    keystorePassword,
                    keystoreSigner,
                    dstdn,
                    service,
                    ticketTime
            );
/* testing only */
//            return new LoginCmsResponseDto(
//                    new LoginCmsResponseDto.HeaderDto("","","","",""),
//                    new LoginCmsResponseDto.CredentialsDto("","")
//
//            );

            // Obtener el endpoint base dinámico de WSAA
            Wsfe.overrideWsaaBase(Wsfe.TEST_WSAA_API_BASE); // Usar TEST_WSAA_API_BASE para pruebas
            String endpoint = Wsfe.getWsaaBase() + "/ws/services/LoginCms"; // Concatenar la ruta específica

            // Invocar el WSAA
            WsfeClient client = new WsfeClient(loginTicketRequestXmlCms);
            client.loginService();

            return client.loginService().invokeWsaa(loginTicketRequestXmlCms, endpoint);
            //return soapClientService.invokeWsaa(loginTicketRequestXmlCms, endpoint);
            //return ArcaWSAAClient.invoke_wsaa(loginTicketRequestXmlCms, endpoint);
            //return "HOLA HOLAA";
            //return generateJsonConfig();

        } catch (Exception e) {
            // Manejo de excepciones
            throw new RuntimeException("Error al invocar el WSAA", e);
        }
    }

    /**
     * Generates a JSON representation of the configuration values from the application.properties file.
     * <p>
     * This method is intended for testing purposes only to verify that the configuration
     * values are being loaded correctly.
     * </p>
     * <p>
     * <b>WARNING:</b> This method should <b>NOT</b> be used in production as it exposes sensitive
     * information such as keystore paths and passwords.
     * </p>
     *
     * @return a JSON string containing the configuration values.
     * @throws RuntimeException if there is an error while generating the JSON.
     */
    private String generateJsonConfig() {
        // Crear un mapa con las propiedades
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", endpoint);
        config.put("keystorePath", keystorePath);
        config.put("keystorePassword", keystorePassword);
        config.put("keystoreSigner", keystoreSigner);
        config.put("dstdn", dstdn);
        config.put("ticketTime", ticketTime);
        config.put("service", service);

        try {
            // Convertir el mapa a JSON usando ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al generar JSON de configuración", e);
        }
    }
}
