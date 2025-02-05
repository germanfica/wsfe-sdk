package com.germanfica.wsfe.controller;

import com.germanfica.wsfe.dto.LoginCmsResponseDto;
import com.germanfica.wsfe.service.ArcaWSAAClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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

    //@GetMapping(value = "/wsaa/invoke", produces = "application/soap+xml")

    @ResponseBody
    @GetMapping(value = "/wsaa/invoke")
    public ResponseEntity<LoginCmsResponseDto> invokeWsaa() {
        //return "HOLAAA";
        return ResponseEntity.ok(arcaWSAAClientService.invokeWsaa());
    }
}