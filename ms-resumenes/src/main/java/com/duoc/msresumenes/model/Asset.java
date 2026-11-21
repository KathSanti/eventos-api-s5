package com.duoc.msresumenes.model;

import lombok.Builder;
import lombok.Value;

// lo que responde la API al subir o modificar, datos de un archivo guardado en S3,

@Value
@Builder
public class Asset {

    //Clave completa en el bucket, por ejemplo "15/resumen-compra-15.txt
    String key;

    // Carpeta dentro del bucket = número del resumen, por ejemplo "15/
    String carpeta;

    String nombreArchivo;

    String contentType;

    long tamanoBytes;
}
