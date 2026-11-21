package com.duoc.msresumenes.exception;

/** ms-compras no respondió o respondió con un error (503: el servicio del que dependemos no está disponible). */
public class ServicioComprasException extends RuntimeException {
    public ServicioComprasException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
