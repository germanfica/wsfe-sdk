package com.germanfica.wsfe.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import com.google.gson.Gson;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utilidad para extraer datos de un XML sin modificar su estructura.
 *
 * <p>
 * Esta clase se creó específicamente para resolver la problemática del servicio
 * WSAA de ARCA (AFIP), el cual devuelve un XML firmado dentro de un String en
 * lugar de una estructura tipada en SOAP. Esto se debe a que cualquier
 * re-serialización automática de SOAP podría alterar la firma digital.
 * </p>
 *
 * <p>
 * En lugar de deserializar el XML a un objeto Java (lo que podría romper la firma),
 * esta clase utiliza XPath para extraer valores directamente del documento XML sin
 * modificarlo. Así garantizamos que la firma digital siga siendo válida.
 * </p>
 *
 * <p>
 * Uso recomendado:
 * </p>
 * <pre>
 * {@code
 * XMLExtractor extractor = new XMLExtractor(xmlResponse);
 * String token = extractor.extractValue("/loginTicketResponse/credentials/token");
 * XMLExtractor.LoginTicketData data = extractor.extractLoginTicketData();
 * System.out.println("LoginTicketData: " + data);
 * }
 * </pre>
 *
 * @author German Fica
 * @version 1.0
 */
public class XMLExtractor {

    private final Document document;
    private final XPath xpath;

    /**
     * Constructor que inicializa el extractor con un XML en formato String.
     *
     * @param xmlResponse El XML en formato String.
     * @throws Exception Si hay un error al procesar el XML.
     */
    public XMLExtractor(String xmlResponse) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

        this.xpath = XPathFactory.newInstance().newXPath();
    }

    /**
     * Extrae el valor de un nodo XML utilizando una expresión XPath.
     *
     * @param expression La expresión XPath para el nodo deseado.
     * @return El valor del nodo como String.
     * @throws Exception Si hay un error al evaluar XPath.
     */
    public String extractValue(String expression) throws XPathExpressionException {
        XPathExpression expr = xpath.compile(expression);
        return (String) expr.evaluate(document, XPathConstants.STRING);
    }

    /**
     * Extrae únicamente el valor del sign desde el XML.
     *
     * @return El valor del sign como String.
     * @throws Exception Si ocurre un error al extraer el valor.
     */
    public String extractSign() throws Exception {
        return extractValue("/loginTicketResponse/credentials/sign");
    }

    /**
     * Extrae únicamente el valor del token desde el XML.
     *
     * @return El valor del token como String.
     * @throws Exception Si ocurre un error al extraer el valor.
     */
    public String extractToken() throws Exception {
        return extractValue("/loginTicketResponse/credentials/token");
    }

    /**
     * Método de utilidad para extraer valores comunes de un `loginTicketResponse`.
     *
     * @return Un objeto `LoginTicketData` con los valores extraídos.
     * @throws Exception Si ocurre un error al extraer los datos.
     */
    public LoginTicketData extractLoginTicketData() throws Exception {
        return new LoginTicketData(
                extractValue("/loginTicketResponse/credentials/token"),
                extractValue("/loginTicketResponse/credentials/sign"),
                extractValue("/loginTicketResponse/header/generationTime"),
                extractValue("/loginTicketResponse/header/expirationTime")
        );
    }

    /**
     * Clase interna que representa los datos extraídos del loginTicketResponse.
     */
    public static class LoginTicketData {
        public final String token;
        public final String sign;
        public final String generationTime;
        public final String expirationTime;

        public LoginTicketData(String token, String sign, String generationTime, String expirationTime) {
            this.token = token;
            this.sign = sign;
            this.generationTime = generationTime;
            this.expirationTime = expirationTime;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
            // return new Gson().newBuilder().disableHtmlEscaping().create().toJson(this);
        }
    }
}
