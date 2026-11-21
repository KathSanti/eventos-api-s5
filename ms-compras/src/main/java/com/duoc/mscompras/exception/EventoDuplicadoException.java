package com.duoc.mscompras.exception;

public class EventoDuplicadoException extends RuntimeException {
    public EventoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}