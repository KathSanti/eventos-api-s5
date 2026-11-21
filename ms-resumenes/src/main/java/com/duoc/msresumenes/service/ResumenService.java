package com.duoc.msresumenes.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.duoc.msresumenes.client.ComprasClient;
import com.duoc.msresumenes.dto.ArchivoResumen;
import com.duoc.msresumenes.dto.CompraDto;
import com.duoc.msresumenes.exception.ArchivoInvalidoException;
import com.duoc.msresumenes.exception.RecursoNoEncontradoException;
import com.duoc.msresumenes.exception.SolicitudInvalidaException;
import com.duoc.msresumenes.model.Asset;
import com.duoc.msresumenes.repository.S3Repository;

/**
 * Lógica de los 5 endpoints de la actividad.
 * La clave en S3 la arma el SERVIDOR: {numero}/resumen-compra-{numero}.txt
 * (la "carpeta" del bucket es el número del resumen, como pide el caso).
 */
@Service
public class ResumenService {

    private static final long TAMANO_MAXIMO = 1024 * 1024; // 1 MB

    private final ComprasClient comprasClient;
    private final S3Repository s3Repository;
    private final GeneradorResumen generador;

    public ResumenService(ComprasClient comprasClient, S3Repository s3Repository, GeneradorResumen generador) {
        this.comprasClient = comprasClient;
        this.s3Repository = s3Repository;
        this.generador = generador;
    }

    /** 1) Genera el resumen como archivo físico (se descarga al computador, no toca S3). */
    public ArchivoResumen generarArchivo(Long numero) {
        validarNumero(numero);
        CompraDto compra = comprasClient.obtenerCompra(numero);
        return new ArchivoResumen(generador.nombreArchivo(numero), GeneradorResumen.CONTENT_TYPE, generador.generar(compra));
    }

    /** 2) Genera el resumen y lo sube a S3 en la carpeta {numero}/. */
    public Asset subirAS3(Long numero) {
        ArchivoResumen archivo = generarArchivo(numero);
        return s3Repository.subir(claveDe(numero), archivo.contenido(), archivo.contentType());
    }

    /** 3) MODIFICAR: reemplaza el archivo de S3 por uno corregido (form-data, campo "file"). */
    public Asset modificarEnS3(Long numero, MultipartFile archivo) {
        String key = claveExistente(numero);
        validarArchivo(archivo);
        try {
            return s3Repository.subir(key, archivo.getBytes(), GeneradorResumen.CONTENT_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo recibido", e);
        }
    }

    /** 4) DESCARGAR el archivo guardado en S3. */
    public ArchivoResumen descargarDeS3(Long numero) {
        String key = claveExistente(numero);
        return new ArchivoResumen(generador.nombreArchivo(numero), GeneradorResumen.CONTENT_TYPE, s3Repository.descargar(key));
    }

    /** 5) BORRAR el archivo de S3. */
    public void eliminarDeS3(Long numero) {
        s3Repository.eliminar(claveExistente(numero));
    }

    // ---------------------------------------------------------------------

    private String claveDe(Long numero) {
        return numero + "/" + generador.nombreArchivo(numero);
    }

    /** S3 no avisa si borras o lees algo que no existe: lo revisamos para responder 404. */
    private String claveExistente(Long numero) {
        validarNumero(numero);
        String key = claveDe(numero);
        if (!s3Repository.existe(key)) {
            throw new RecursoNoEncontradoException(
                    "El resumen N° " + numero + " no está en S3. Súbelo primero con POST /api/resumenes/" + numero + "/s3");
        }
        return key;
    }

    private void validarNumero(Long numero) {
        if (numero == null || numero < 1) {
            throw new SolicitudInvalidaException("El número de resumen debe ser un entero mayor que 0");
        }
    }

    /** Solo .txt, no vacío y hasta 1 MB. */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoInvalidoException("Debes adjuntar un archivo en el campo 'file'");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new ArchivoInvalidoException("El archivo supera el máximo de 1 MB");
        }
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        String tipo = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
        if (!tipo.startsWith("text/plain") && !nombre.endsWith(".txt")) {
            throw new ArchivoInvalidoException("Solo se aceptan archivos de texto (.txt)");
        }
    }
}
