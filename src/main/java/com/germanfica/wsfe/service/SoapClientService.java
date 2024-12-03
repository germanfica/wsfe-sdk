package com.germanfica.wsfe.service;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.dto.LoginCmsResponseDto;
import com.germanfica.wsfe.exception.ApiException;
//import generated.LoginCmsResponseType;
import com.germanfica.wsfe.utils.ArcaDateTimeUtils;
import com.germanfica.wsfe.utils.ArcaWSAAUtils;
import generated.LoginTicketResponseType;
import gov.afip.desein.dvadac.sua.view.wsaa.LoginCmsResponse;
import gov.afip.desein.dvadac.sua.view.wsaa.ObjectFactory;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;

import static com.germanfica.wsfe.utils.ArcaWSAAUtils.convertXmlToObject;


@Service
public class SoapClientService {

    private final WebServiceTemplate webServiceTemplate;

    public SoapClientService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public LoginCmsResponseDto invokeWsaa(byte[] loginTicketRequestXmlCms, String endpoint) {
        try {
            // Configurar marshaller y crear la solicitud
//            var marshaller = new org.springframework.oxm.jaxb.Jaxb2Marshaller();
//            marshaller.setPackagesToScan("gov.afip.desein.dvadac.sua.view.wsaa");
//            webServiceTemplate.setMarshaller(marshaller);
//            webServiceTemplate.setUnmarshaller(marshaller);
//
//            var factory = new ObjectFactory();
//            var request = factory.createLoginCms();
//            request.setIn0(Base64.encodeBase64String(loginTicketRequestXmlCms));
//
//            // Enviar solicitud y obtener respuesta
//            var response = (LoginCmsResponse) webServiceTemplate.marshalSendAndReceive(
//                    endpoint, request, new SoapActionCallback("urn:loginCms")
//            );

//            String xmlString = """
//            <LoginCmsResponseDto>
//                <header>
//                    <source>AFIP</source>
//                    <destination>User</destination>
//                    <uniqueId>123456</uniqueId>
//                    <generationTime>2024-12-02T10:00:00</generationTime>
//                    <expirationTime>2024-12-02T18:00:00</expirationTime>
//                </header>
//                <credentials>
//                    <token>abcd1234</token>
//                    <sign>efgh5678</sign>
//                </credentials>
//            </LoginCmsResponseDto>
//        """;

            String xmlString = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
             <loginTicketResponse version="1.0">
             <header>
             <source>CN=wsaahomo, O=AFIP, C=AR,
            SERIALNUMBER=CUIT 33693450239</source>
             <destination>SERIALNUMBER=CUIT 20190178154,
             CN=glarriera20190903</destination>
             <uniqueId>3866895167</uniqueId>
             <generationTime>2019-09-26T13:56:14.467-03:00</generationTime>
             <expirationTime>2019-09-27T01:56:14.467-03:00</expirationTime>
             </header>
             <credentials>
             <token>PD94bWwgdmVyc2lv . . . go8L3Nzbz4K</token>
             <sign>Urp5dbarIb8m5y . . . SEzSeon1W7ys=</sign>
             </credentials>
             </loginTicketResponse>
                    """;

            //String xmlString = response.getLoginCmsReturn();
            System.out.println(xmlString);

            return mapToDto(xmlString);

        } catch (SoapFaultClientException e) {
            // Manejo de errores SOAP específicos
            //log.error("SOAP Fault: {}", e.getMessage());
            throw new ApiException(
                    mapToErrorDto(e), HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            // Manejo de otros errores
            //log.error("Unexpected error: {}", e.getMessage());
            throw new ApiException(
                    new ErrorDto("unexpected_error", "Unexpected error occurred", null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Método para generar un XML con el mensaje de error
    private String generateErrorXml(String errorMessage) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<error>" +
                "<message>" + errorMessage + "</message>" +
                "</error>";
    }

    // Método auxiliar para convertir objetos a XML (opcional, para depuración)
    private String marshallObjectToXml(Object obj) throws JAXBException {
        // Usa el contexto de JAXB para la clase específica
        JAXBContext context = JAXBContext.newInstance(obj.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Usa un OutputStream para capturar el XML
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            marshaller.marshal(obj, outputStream);
        } catch (JAXBException ex) {
            throw new JAXBException("Error al marshallizar el objeto a XML", ex);
        }
        return outputStream.toString(); // Convierte el contenido a String
    }

    // == utils ==
    private LoginCmsResponseDto mapToDto(String xml) throws Exception {
        // Obtener los valores desde la respuesta
        String loginCmsReturn = xml;

        try {
            LoginTicketResponseType responseObj = convertXmlToObject(loginCmsReturn, LoginTicketResponseType.class);
            System.out.println("Header Source: " + responseObj.getHeader().getSource());
            System.out.println("Token: " + responseObj.getCredentials().getToken());

            // Construir el DTO
            return new LoginCmsResponseDto(
                    new LoginCmsResponseDto.HeaderDto(
                            responseObj.getHeader().getSource(), // Extraer del contenido decodificado
                            responseObj.getHeader().getDestination(), // Extraer del contenido decodificado
                            responseObj.getHeader().getUniqueId(), // Extraer del contenido decodificado
                            ArcaDateTimeUtils.formatDateTime(
                                    responseObj.getHeader().getGenerationTime(),
                                    ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL,
                                    ArcaDateTimeUtils.OffsetOption.ES_AR), // Extraer del contenido decodificado
                            ArcaDateTimeUtils.formatDateTime(
                                    responseObj.getHeader().getExpirationTime(),
                                    ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL,
                                    ArcaDateTimeUtils.OffsetOption.ES_AR) // Extraer del contenido decodificado
                    ),
                    new LoginCmsResponseDto.CredentialsDto(
                            responseObj.getCredentials().getToken(), // Extraer del contenido decodificado
                            responseObj.getCredentials().getSign() // Extraer del contenido decodificado
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // TODO: hay que corregir exceptionName y hostname, no deberia estar hardcoded
    private ErrorDto mapToErrorDto(SoapFaultClientException e) {
        // Parsear SOAP Fault al DTO de error
        var faultCode = e.getFaultCode();
        var faultString = e.getFaultStringOrReason();
        var exceptionName = "gov.afip.desein.dvadac.sua.view.wsaa.LoginFault"; // Ajustar según el mensaje
        var hostname = "wsaaext1.homo.afip.gov.ar"; // Ajustar según el contexto

        return new ErrorDto(faultCode != null ? faultCode.getLocalPart().toString() : "unknown_code", faultString, new ErrorDto.ErrorDetailsDto(exceptionName, hostname));
    }
}
