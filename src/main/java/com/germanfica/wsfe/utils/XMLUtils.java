package com.germanfica.wsfe.utils;

import com.germanfica.wsfe.model.LoginTicketRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

public class XMLUtils {

    public static String createLoginTicketRequest(String signerDN, String dstDN, String service, Long ticketTime) {
        LoginTicketRequest request = LoginTicketRequest.create(signerDN, dstDN, service, ticketTime);
        return toXML(request);
    }

//    public static String toXML(Object object) {
//        try {
//            XmlMapper xmlMapper = new XmlMapper();
//            return xmlMapper.writeValueAsString(object);
//        } catch (Exception e) {
//            throw new RuntimeException("Error al convertir objeto a XML", e);
//        }
//    }

    public static String toXML(Object object) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // salida con saltos de línea e indentaciones

            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("Error al serializar a XML", e);
        }
    }
}
