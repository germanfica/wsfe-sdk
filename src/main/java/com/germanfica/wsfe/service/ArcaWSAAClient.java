package com.germanfica.wsfe.service;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
//import javax.xml.rpc.ParameterMode;




import org.bouncycastle.cms.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

//import org.apache.axis2.client.ServiceClient;
//import org.apache.axis2.client.Options;
//import org.apache.axis2.addressing.EndpointReference;
//import org.apache.axiom.om.OMElement;
//import org.apache.axiom.om.OMFactory;
//import org.apache.axiom.om.OMNamespace;
//import org.apache.axiom.om.OMAbstractFactory;
//import org.apache.commons.codec.binary.Base64;

// import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringSource;
import javax.xml.transform.Source;
import org.springframework.ws.client.core.WebServiceTemplate;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

public class ArcaWSAAClient {

    public static String invoke_wsaa(byte[] loginTicketRequestXmlCms, String endpoint) throws Exception {
        // Codificar en Base64 el contenido
        String encodedRequest = java.util.Base64.getEncoder().encodeToString(loginTicketRequestXmlCms);

        // Crear el payload de la solicitud SOAP
        String soapRequest =
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                        "xmlns:wsaa=\"http://wsaa.view.sua.dvadac.desein.afip.gov\">" +
                        "   <soapenv:Header/>" +
                        "   <soapenv:Body>" +
                        "      <wsaa:loginCms>" +
                        "         <wsaa:in0>" + encodedRequest + "</wsaa:in0>" +
                        "      </wsaa:loginCms>" +
                        "   </soapenv:Body>" +
                        "</soapenv:Envelope>";

        // Crear WebServiceTemplate
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();

        // Configurar la solicitud como StreamSource
        StreamSource requestSource = new StreamSource(new StringReader(soapRequest));
        StringWriter responseWriter = new StringWriter();
        StreamResult responseResult = new StreamResult(responseWriter);

        // Enviar solicitud y recibir respuesta
        webServiceTemplate.sendSourceAndReceiveToResult(endpoint, requestSource, responseResult);

        // Extraer el texto de la respuesta SOAP
        String responseXml = responseWriter.toString();

        // (Opcional) Procesar el XML para obtener el valor de "loginTicketResponse"
        String loginTicketResponse = extractLoginTicketResponse(responseXml);

        return loginTicketResponse;
    }

    // Método auxiliar para extraer el valor de loginTicketResponse
    private static String extractLoginTicketResponse(String responseXml) {
        // Implementa un parser XML o XPath para extraer el contenido del nodo deseado
        // Ejemplo rápido usando un regex simple (mejor usar un parser robusto para producción)
        String startTag = "<loginCmsReturn>";
        String endTag = "</loginCmsReturn>";
        int start = responseXml.indexOf(startTag) + startTag.length();
        int end = responseXml.indexOf(endTag);
        return responseXml.substring(start, end).trim();
    }

    //
    // Create the CMS Message
    //
    public static byte[] create_cms(String p12file, String p12pass, String signer, String dstDN, String service, Long TicketTime) {

        PrivateKey pKey = null;
        X509Certificate pCertificate = null;
        byte[] asn1_cms = null;
        Store certStore = null;
        String LoginTicketRequest_xml;
        String SignerDN = null;

        try {
            // Cargar el keystore del archivo PKCS#12
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream p12stream = new FileInputStream(p12file);
            ks.load(p12stream, p12pass.toCharArray());
            p12stream.close();

            // Obtener la clave privada y el certificado del keystore
            pKey = (PrivateKey) ks.getKey(signer, p12pass.toCharArray());
            pCertificate = (X509Certificate) ks.getCertificate(signer);
            SignerDN = pCertificate.getSubjectDN().toString();

            // Crear una lista de certificados
            List<X509Certificate> certList = new ArrayList<>();
            certList.add(pCertificate);

            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }

            // Crear un Store para los certificados
            certStore = new JcaCertStore(certList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Crear el XML del LoginTicketRequest
        LoginTicketRequest_xml = create_LoginTicketRequest(SignerDN, dstDN, service, TicketTime);

        try {
            // Generar el firmante de contenido
            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA1withRSA")
                    .setProvider("BC")
                    .build(pKey);

            // Configurar la información del firmante
            JcaSignerInfoGeneratorBuilder signerInfoBuilder = new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
            );

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            gen.addSignerInfoGenerator(
                    signerInfoBuilder.build(contentSigner, new X509CertificateHolder(pCertificate.getEncoded()))
            );

            // Añadir el Store de certificados
            gen.addCertificates(certStore);

            // Añadir los datos XML al CMS
            CMSProcessableByteArray data = new CMSProcessableByteArray(LoginTicketRequest_xml.getBytes());

            // Generar el mensaje CMS
            CMSSignedData signedData = gen.generate(data, true);

            // Obtener los bytes ASN.1
            asn1_cms = signedData.getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return asn1_cms;
    }

    //
    // Create XML Message for AFIP wsaa
    //
    public static String create_LoginTicketRequest(String SignerDN, String dstDN, String service, Long TicketTime) {

        String LoginTicketRequest_xml;

        // Obtener la fecha actual
        Date GenTime = new Date();

        // Configurar tiempos de generación y expiración con un desfase de 10 minutos
        GregorianCalendar gentime = new GregorianCalendar();
        GregorianCalendar exptime = new GregorianCalendar();
        gentime.setTime(new Date(GenTime.getTime() - 10 * 60 * 1000)); // Resta 10 minutos
        exptime.setTime(new Date(GenTime.getTime() + 10 * 60 * 1000)); // Suma 10 minutos

        // Convertir GregorianCalendar a XMLGregorianCalendar
        DatatypeFactory datatypeFactory = null;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        XMLGregorianCalendar XMLGenTime = datatypeFactory.newXMLGregorianCalendar(gentime);
        XMLGregorianCalendar XMLExpTime = datatypeFactory.newXMLGregorianCalendar(exptime);

        // Generar el UniqueId
        String UniqueId = String.valueOf(GenTime.getTime() / 1000);

        // Construir el XML del LoginTicketRequest
        LoginTicketRequest_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<loginTicketRequest version=\"1.0\">"
                + "<header>"
                + "<source>" + SignerDN + "</source>"
                + "<destination>" + dstDN + "</destination>"
                + "<uniqueId>" + UniqueId + "</uniqueId>"
                + "<generationTime>" + XMLGenTime.toXMLFormat().split("\\.")[0] + "</generationTime>"
                + "<expirationTime>" + XMLExpTime.toXMLFormat().split("\\.")[0] + "</expirationTime>"
                + "</header>"
                + "<service>" + service + "</service>"
                + "</loginTicketRequest>";

        return LoginTicketRequest_xml;
    }
}
