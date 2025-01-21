package com.germanfica.wsfe.model.soap.login;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "in0"
})
@XmlRootElement(name = "loginCms")
public class LoginCms {
    @XmlElement(required = true)
    protected String in0;
}
