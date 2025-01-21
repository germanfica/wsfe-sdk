package com.germanfica.wsfe.model.soap.login;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "loginCmsReturn"
})
@XmlRootElement(name = "loginCmsResponse")
public class LoginCmsResponse {
    @XmlElement(required = true)
    protected String loginCmsReturn;
}
