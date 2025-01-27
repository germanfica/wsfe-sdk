package com.germanfica.wsfe.service;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.dto.LoginCmsResponseDto;
import com.germanfica.wsfe.exception.ApiException;

import com.germanfica.wsfe.exception.XmlMappingException;

import com.germanfica.wsfe.model.soap.loginticket.LoginTicketResponseType;
import com.germanfica.wsfe.net.ApiRequest;
import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.HttpStatus;
import com.germanfica.wsfe.net.SoapRequestHandler;
import com.germanfica.wsfe.utils.ArcaDateTimeUtils;
import com.sun.xml.messaging.saaj.soap.Envelope;
import org.apache.commons.codec.binary.Base64;


import java.util.Map;

public final class LoginService extends ApiService {
    private static final String NAMESPACE = "http://wsaa.view.sua.dvadac.desein.afip.gov.ar/";
    private static final String OPERATION = "loginCms";
    private static final String SOAP_ACTION = "urn:loginCms";

    public LoginService(SoapRequestHandler soapRequestHandler) {
        super(soapRequestHandler);
    }

    public LoginCmsResponseDto invokeWsaa(byte[] loginTicketRequestXmlCms, String endpoint) throws Exception {

        ApiRequest request = new ApiRequest(
                SOAP_ACTION,
                loginTicketRequestXmlCms,
                NAMESPACE,
                OPERATION,
                Map.of("in0", Base64.encodeBase64String(loginTicketRequestXmlCms)),
                endpoint,
                LoginTicketResponseType.class
        );

//        System.out.println(Map.of("in0", Base64.encodeBase64String(loginTicketRequestXmlCms)));

        // success: en caso de éxito
//            LoginTicketResponseType success = this.request(request, LoginTicketResponseType.class);
        // error: en caso de error
//            SoapEnvelope error = this.request(request, SoapEnvelope.class);
//
//            String xmlResponse = """
//<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
//   <soapenv:Body>
//      <soapenv:Fault>
//         <faultcode xmlns:ns1="http://xml.apache.org/axis/">ns1:coe.alreadyAuthenticated</faultcode>
//         <faultstring>El CEE ya posee un TA valido para el acceso al WSN solicitado</faultstring>
//         <detail>
//            <ns2:exceptionName xmlns:ns2="http://xml.apache.org/axis/">gov.afip.desein.dvadac.sua.view.wsaa.LoginFault</ns2:exceptionName>
//            <ns3:hostname xmlns:ns3="http://xml.apache.org/axis/">wsaaext0.homo.afip.gov.ar</ns3:hostname>
//         </detail>
//      </soapenv:Fault>
//   </soapenv:Body>
//</soapenv:Envelope>
//            """;
//
//            JAXBContext jaxbContext = JAXBContext.newInstance(SoapEnvelope.class);
//            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//
//            StringReader reader = new StringReader(xmlResponse);
//            SoapEnvelope envelope = (SoapEnvelope) unmarshaller.unmarshal(reader);
//
//            System.out.println("Fault Code: " + envelope.getBody().getFault().getFaultCode());
//            System.out.println("Fault String: " + envelope.getBody().getFault().getFaultString());
//            System.out.println("Exception Name: " + envelope.getBody().getFault().getDetail().getExceptionName());
//            System.out.println("Hostname: " + envelope.getBody().getFault().getDetail().getHostname());



        // Mapear al DTO
        //return new LoginCmsResponseDto(null,null);
        return postProcessDto(this.request(request, LoginTicketResponseType.class));
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
