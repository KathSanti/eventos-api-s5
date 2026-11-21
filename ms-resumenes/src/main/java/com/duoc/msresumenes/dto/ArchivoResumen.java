package com.duoc.msresumenes.dto;

/** Un archivo listo para devolverse como descarga: nombre, tipo y contenido. */
public record ArchivoResumen(
        String nombreArchivo,
        String contentType,
        byte[] contenido
) {}
