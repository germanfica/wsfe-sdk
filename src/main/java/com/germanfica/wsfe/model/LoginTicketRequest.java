package com.germanfica.wsfe.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.germanfica.wsfe.time.ArcaDateTime;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "loginTicketRequest")
@JacksonXmlRootElement(localName = "loginTicketRequest")
@XmlType(propOrder = {"version", "header", "service"})
public class LoginTicketRequest {

    private String version = "1.0";
    private Header header;
    private String service;

    @XmlAttribute(name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "header")
    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    @XmlElement(name = "service")
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @XmlType(propOrder = {"source", "destination", "uniqueId", "generationTime", "expirationTime"})
    public static class Header {
        private String source;
        private String destination;
        private String uniqueId;
        private String generationTime;
        private String expirationTime;

        @XmlElement(name = "source")
        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        @XmlElement(name = "destination")
        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        @XmlElement(name = "uniqueId")
        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        @XmlElement(name = "generationTime")
        public String getGenerationTime() {
            return generationTime;
        }

        public void setGenerationTime(String generationTime) {
            this.generationTime = generationTime;
        }

        @XmlElement(name = "expirationTime")
        public String getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(String expirationTime) {
            this.expirationTime = expirationTime;
        }
    }

    public static LoginTicketRequest create(String source, String destination, String service, long ticketTimeMillis) {
        LoginTicketRequest request = new LoginTicketRequest();

        Header header = new Header();
        header.setSource(source);
        header.setDestination(destination);
        header.setUniqueId(String.valueOf(System.currentTimeMillis() / 1000));

        ArcaDateTime generationTime = ArcaDateTime.now();
        ArcaDateTime expirationTime = ArcaDateTime.now().plusMinutes(10);

        header.setGenerationTime(generationTime.toString());
        header.setExpirationTime(expirationTime.toString());

        request.setHeader(header);
        request.setService(service);

        return request;
    }
}
