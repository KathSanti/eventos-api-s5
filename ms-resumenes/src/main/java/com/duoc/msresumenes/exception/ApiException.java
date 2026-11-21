package com.duoc.msresumenes.exception;

import org.springframework.http.HttpStatus;


public class ApiException extends RuntimeException {

    private final HttpStatus status;

    private ApiException(HttpStatus status, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException noEncontrado(String mensaje) {          
        return new ApiException(HttpStatus.NOT_FOUND, mensaje, null);
    }

    public static ApiException solicitudInvalida(String mensaje) {     
        return new ApiException(HttpStatus.BAD_REQUEST, mensaje, null);
    }

    public static ApiException prohibido(String mensaje) {             
        return new ApiException(HttpStatus.FORBIDDEN, mensaje, null);
    }

    public static ApiException servicioNoDisponible(String mensaje, Throwable causa) {  // 503
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, mensaje, causa);
    }
}