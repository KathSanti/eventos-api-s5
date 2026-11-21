package com.duoc.mscompras.exception;

import org.springframework.http.HttpStatus;

// Una sola excepción para todos los errores 
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    private ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

 
    public static ApiException noEncontrado(String mensaje) {   // 404
        return new ApiException(HttpStatus.NOT_FOUND, mensaje);
    }

    public static ApiException conflicto(String mensaje) {      // 409
        return new ApiException(HttpStatus.CONFLICT, mensaje);
    }

    public static ApiException prohibido(String mensaje) {      // 403
        return new ApiException(HttpStatus.FORBIDDEN, mensaje);
    }
}