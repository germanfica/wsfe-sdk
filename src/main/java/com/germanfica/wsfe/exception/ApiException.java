package com.germanfica.wsfe.exception;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.net.HttpStatus;
import lombok.Getter;


@Getter
public class ApiException extends Exception {
    private final ErrorDto errorDto;
    private final HttpStatus httpStatus;

    public ApiException(ErrorDto errorDto, HttpStatus httpStatus) {
        super(errorDto.getFaultString());
        this.errorDto = errorDto;
        this.httpStatus = httpStatus;
    }
}
