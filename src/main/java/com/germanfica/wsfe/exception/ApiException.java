package com.germanfica.wsfe.exception;

import com.germanfica.wsfe.dto.ErrorDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorDto errorDto;
    private final HttpStatus httpStatus;

    public ApiException(ErrorDto errorDto, HttpStatus httpStatus) {
        super(errorDto.getFaultString());
        this.errorDto = errorDto;
        this.httpStatus = httpStatus;
    }
}
