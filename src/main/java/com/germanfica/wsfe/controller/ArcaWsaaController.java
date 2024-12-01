package com.germanfica.wsfe.controller;

import com.germanfica.wsfe.service.ArcaWSAAClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArcaWsaaController {
    // == fields ==
    private final ArcaWSAAClientService arcaWSAAClientService;

    // == constructors ==
    @Autowired
    public ArcaWsaaController(ArcaWSAAClientService arcaWSAAClientService) {
        this.arcaWSAAClientService = arcaWSAAClientService;
    }

    @GetMapping("/wsaa/invoke")
    public String invokeWsaa() {
        //return "HOLAAA";
        return arcaWSAAClientService.invokeWsaa();
    }
}