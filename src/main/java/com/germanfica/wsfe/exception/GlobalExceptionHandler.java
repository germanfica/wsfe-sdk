package com.germanfica.wsfe.exception;

import com.germanfica.wsfe.dto.ErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorDto> handleApiException(ApiException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getErrorDto());
    }
}
