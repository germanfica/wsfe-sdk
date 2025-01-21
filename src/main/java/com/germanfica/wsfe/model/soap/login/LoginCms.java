package com.germanfica.wsfe.model.soap.login;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "in0"
})
@XmlRootElement(name = "loginCms")
public class LoginCms {

    @XmlElement(required = true)
    protected String in0;

    public String getIn0() {
        return in0;
    }

    public void setIn0(String value) {
        this.in0 = value;
    }
}
