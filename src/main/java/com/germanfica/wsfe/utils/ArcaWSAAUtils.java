package com.germanfica.wsfe.utils;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.germanfica.wsfe.model.LoginTicketRequest;
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

    /**
     * Converts an XML string to an object of the specified type.
     *
     * @param xmlString the XML string to convert
     * @param clazz     the class type of the object
     * @param <T>       the type parameter
     * @return the deserialized object of type T
     * @throws Exception if an error occurs during deserialization
     */
    public static <T> T convertXmlToObject(String xmlString, Class<T> clazz) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(xmlString, clazz);
    }
}
