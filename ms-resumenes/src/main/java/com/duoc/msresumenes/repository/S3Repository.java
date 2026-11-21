package com.duoc.msresumenes.repository;

import com.duoc.msresumenes.model.Asset;

/**
 * Operaciones sobre archivos en el bucket S3. Es la ÚNICA capa que conoce S3.
 * El bucket NO se recibe como parámetro: viene de la configuración (app.s3.bucket).
 */
public interface S3Repository {

    // Sube o reemplaza, si la clave ya existe) un archivo.
    Asset subir(String key, byte[] contenido, String contentType);

    byte[] descargar(String key);

    boolean existe(String key);

    void eliminar(String key);
}
