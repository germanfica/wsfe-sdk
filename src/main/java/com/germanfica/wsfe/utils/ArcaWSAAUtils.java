package com.germanfica.wsfe.utils;

import java.io.FileInputStream;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.germanfica.wsfe.exception.XmlMappingException;
import com.germanfica.wsfe.model.LoginTicketRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
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

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class ArcaWSAAUtils {
    //
    // Create the CMS Message
    //
    /**
     * This method creates a CMS (Cryptographic Message Syntax) signed message that encapsulates
     * a LoginTicketRequest XML, which is required for accessing services in the ARCA (AFIP) system.
     * It uses a PKCS#12 keystore file (.p12) containing the private key and certificate to sign the
     * request. The resulting CMS message is used for authentication in the ARCA web service.
     *
     * The process includes:
     * 1. Loading the PKCS#12 keystore to extract the private key and certificate.
     * 2. Creating the LoginTicketRequest XML with the provided details.
     * 3. Signing the XML data using the private key and generating a CMS message.
     * 4. Returning the CMS message as ASN.1 encoded data.
     *
     * @param p12file the path to the PKCS#12 keystore file
     * @param p12pass the password for the PKCS#12 keystore
     * @param signer  the alias of the signer in the keystore
     * @param dstDN   the Distinguished Name of the destination
     * @param service the service name
     * @param TicketTime the ticket expiration time
     * @return the ASN.1 encoded CMS message
     */
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

        System.out.println(LoginTicketRequest_xml);

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
        LoginTicketRequest request = LoginTicketRequest.create(SignerDN, dstDN, service, TicketTime);
        return XMLUtils.toXML(request);
    }

//    /**
//     * Converts an XML string to an object of the specified type using JAXBContext.
//     *
//     * @param xmlString the XML string to convert
//     * @param clazz     the class type of the object
//     * @param <T>       the type parameter
//     * @return the deserialized object of type T
//     * @throws Exception if an error occurs during deserialization
//     */
//    public static <T> T convertXmlToObjectJAXB(String xmlString, Class<T> clazz) throws Exception {
//        JAXBContext jaxbContext = JAXBContext.newInstance(clazz); // Initialize JAXBContext with the class
//        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); // Create an Unmarshaller instance
//        StringReader reader = new StringReader(xmlString); // Wrap the XML string in a StringReader
//        return clazz.cast(unmarshaller.unmarshal(reader)); // Deserialize the XML into an object of type T
//    }
//
//    /**
//     * Converts an XML string to an object of the specified type.
//     *
//     * @param xmlString the XML string to convert
//     * @param clazz     the class type of the object
//     * @param <T>       the type parameter
//     * @return the deserialized object of type T
//     * @throws Exception if an error occurs during deserialization
//     */
//    public static <T> T convertXmlToObject(String xmlString, Class<T> clazz) throws XmlMappingException {
//        XmlMapper xmlMapper = new XmlMapper();
//        try {
//            return xmlMapper.readValue(xmlString, clazz);
//        } catch (Exception e) {
//            throw new XmlMappingException("Error mapping XML to object", xmlString, e);
//        }
//    }
}
