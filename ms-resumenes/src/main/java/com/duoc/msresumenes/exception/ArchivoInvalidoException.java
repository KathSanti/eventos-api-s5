package com.duoc.msresumenes.exception;

/** El archivo enviado para modificar el resumen no cumple las reglas (400) */
public class ArchivoInvalidoException extends RuntimeException {
    public ArchivoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
