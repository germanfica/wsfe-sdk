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

import java.util.Map;

@Service
public class SoapClientService extends SoapService {
    private static final String NAMESPACE = "http://wsaa.view.sua.dvadac.desein.afip.gov.ar/";
    private static final String OPERATION = "loginCms";
    private static final String SOAP_ACTION = "urn:loginCms";

    public LoginCmsResponseDto invokeWsaa(byte[] loginTicketRequestXmlCms, String endpoint) {
        try {
            // Crear mensaje SOAP
            SOAPMessage soapMessage = createSoapMessage(
                    SOAP_ACTION,
                    loginTicketRequestXmlCms,
                    NAMESPACE,
                    OPERATION,
                    Map.of("in0", Base64.encodeBase64String(loginTicketRequestXmlCms))
            );

            // Enviar solicitud
            SOAPMessage soapResponse = sendSoapRequest(soapMessage, endpoint);

            // Procesar respuesta
            String xmlResponse = extractResponse(soapResponse);

            LoginTicketResponseType responseDto = mapToDto(xmlResponse, LoginTicketResponseType.class);

            // Mapear al DTO
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
