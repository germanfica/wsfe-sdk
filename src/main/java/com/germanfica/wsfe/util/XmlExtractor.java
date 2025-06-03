package com.germanfica.wsfe.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

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
 * System.out.println("Token: " + token);
 * }
 * </pre>
 *
 * @author German Fica
 * @version 1.0
 */
public class XmlExtractor {

    private final Document document;
    private final XPath xpath;

    /**
     * Constructor que inicializa el extractor con un XML en formato String.
     *
     * @param xmlResponse El XML en formato String.
     * @throws Exception Si hay un error al procesar el XML.
     */
    public XmlExtractor(String xmlResponse) throws ParserConfigurationException, IOException, SAXException {
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
}
