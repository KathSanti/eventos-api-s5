package com.duoc.msresumenes.repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.duoc.msresumenes.model.Asset;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;

//Implementación con S3Template 
@Repository
public class S3RepositoryImpl implements S3Repository {

    private final S3Template s3Template;
    private final String bucket;

    public S3RepositoryImpl(S3Template s3Template, @Value("${app.s3.bucket}") String bucket) {
        this.s3Template = s3Template;
        this.bucket = bucket;
    }

    @Override
    public Asset subir(String key, byte[] contenido, String contentType) {
        ObjectMetadata metadata = ObjectMetadata.builder().contentType(contentType).build();
        // Misma clave = S3 reemplaza el archivo (con Versioning, la versión anterior queda guardada).
        s3Template.upload(bucket, key, new ByteArrayInputStream(contenido), metadata);
        return Asset.builder()
                .key(key)
                .carpeta(key.substring(0, key.lastIndexOf('/') + 1))
                .nombreArchivo(key.substring(key.lastIndexOf('/') + 1))
                .contentType(contentType)
                .tamanoBytes(contenido.length)
                .build();
    }

    @Override
    public byte[] descargar(String key) {
        try (InputStream in = s3Template.download(bucket, key).getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo '" + key + "' desde S3", e);
        }
    }

    @Override
    public boolean existe(String key) {
        return s3Template.objectExists(bucket, key);
    }

    @Override
    public void eliminar(String key) {
        s3Template.deleteObject(bucket, key);
    }
}
