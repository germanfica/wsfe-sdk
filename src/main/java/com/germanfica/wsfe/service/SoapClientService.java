package com.germanfica.wsfe.service;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.dto.LoginCmsResponseDto;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.HttpStatus;
import com.germanfica.wsfe.utils.ArcaDateTimeUtils;
import generated.LoginTicketResponseType;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import static com.germanfica.wsfe.utils.ArcaWSAAUtils.convertXmlToObject;
import jakarta.xml.soap.*;

@Service
public class SoapClientService {

    public LoginCmsResponseDto invokeWsaa(byte[] loginTicketRequestXmlCms, String endpoint) {
        try {
            // Crear conexión SOAP
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Crear mensaje SOAP
            SOAPMessage soapMessage = createSoapMessage(loginTicketRequestXmlCms);

            // Enviar solicitud y recibir respuesta
            SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);

            // Procesar la respuesta SOAP
            String xmlResponse = extractResponse(soapResponse);
            System.out.println("Response: " + xmlResponse);

            // Mapear respuesta al DTO
            return mapToDto(xmlResponse);
        } catch (SOAPException | JAXBException e) {
            throw new ApiException(
                    new ErrorDto("soap_error", e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            throw new ApiException(
                    new ErrorDto("unexpected_error", "Unexpected error occurred", null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private SOAPMessage createSoapMessage(byte[] loginTicketRequestXmlCms) throws SOAPException {
        // Crear mensaje SOAP
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        // Crear el cuerpo del mensaje
        SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPElement loginCms = body.addChildElement("loginCms", "ns1", "http://wsaa.view.sua.dvadac.desein.afip.gov.ar/");
        loginCms.addChildElement("in0").addTextNode(Base64.encodeBase64String(loginTicketRequestXmlCms));

        // Agregar encabezado SOAPAction
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", "urn:loginCms");

        soapMessage.saveChanges();
        return soapMessage;
    }

    private String extractResponse(SOAPMessage soapResponse) throws SOAPException {
        SOAPBody responseBody = soapResponse.getSOAPBody();
        if (responseBody.hasFault()) {
            throw new SOAPException("SOAP Fault: " + responseBody.getFault().getFaultString());
        }

        return responseBody.getElementsByTagName("loginCmsReturn").item(0).getTextContent();
    }

    private LoginCmsResponseDto mapToDto(String xml) throws Exception {
        LoginTicketResponseType responseObj = convertXmlToObject(xml, LoginTicketResponseType.class);

        return new LoginCmsResponseDto(
                new LoginCmsResponseDto.HeaderDto(
                        responseObj.getHeader().getSource(),
                        responseObj.getHeader().getDestination(),
                        responseObj.getHeader().getUniqueId(),
                        ArcaDateTimeUtils.formatDateTime(responseObj.getHeader().getGenerationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL),
                        ArcaDateTimeUtils.formatDateTime(responseObj.getHeader().getExpirationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL)
                ),
                new LoginCmsResponseDto.CredentialsDto(
                        responseObj.getCredentials().getToken(),
                        responseObj.getCredentials().getSign()
                )
        );
    }
}
