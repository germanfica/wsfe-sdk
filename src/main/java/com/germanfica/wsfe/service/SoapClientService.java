package com.germanfica.wsfe.service;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.dto.LoginCmsResponseDto;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiRequest;
import com.germanfica.wsfe.net.ApiResponseGetter;
import com.germanfica.wsfe.net.BaseApiRequest;
import com.germanfica.wsfe.net.HttpStatus;
import com.germanfica.wsfe.utils.ArcaDateTimeUtils;
import generated.LoginTicketResponseType;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import jakarta.xml.soap.*;

import java.util.Map;

@Service
public class SoapClientService extends SoapService {
    private static final String NAMESPACE = "http://wsaa.view.sua.dvadac.desein.afip.gov.ar/";
    private static final String OPERATION = "loginCms";
    private static final String SOAP_ACTION = "urn:loginCms";

    public LoginCmsResponseDto invokeWsaa(byte[] loginTicketRequestXmlCms, String endpoint) {
        try {
            ApiRequest request = new ApiRequest(
                    SOAP_ACTION,
                    loginTicketRequestXmlCms,
                    NAMESPACE,
                    OPERATION,
                    Map.of("in0", Base64.encodeBase64String(loginTicketRequestXmlCms)),
                    endpoint,
                    LoginTicketResponseType.class
            );

            // Mapear al DTO
            return postProcessDto(request.request(request, LoginTicketResponseType.class));

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

    protected LoginCmsResponseDto postProcessDto(LoginTicketResponseType rawResponse) {
        return new LoginCmsResponseDto(
                new LoginCmsResponseDto.HeaderDto(
                        rawResponse.getHeader().getSource(),
                        rawResponse.getHeader().getDestination(),
                        rawResponse.getHeader().getUniqueId(),
                        ArcaDateTimeUtils.formatDateTime(rawResponse.getHeader().getGenerationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL),
                        ArcaDateTimeUtils.formatDateTime(rawResponse.getHeader().getExpirationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL)
                ),
                new LoginCmsResponseDto.CredentialsDto(
                        rawResponse.getCredentials().getToken(),
                        rawResponse.getCredentials().getSign()
                )
        );
    }
}
