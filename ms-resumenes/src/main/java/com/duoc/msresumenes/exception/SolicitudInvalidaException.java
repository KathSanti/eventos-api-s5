package com.duoc.msresumenes.exception;

/** Un dato de la petición no es válido, por ejemplo un número de resumen menor que 1 (400) */
public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
