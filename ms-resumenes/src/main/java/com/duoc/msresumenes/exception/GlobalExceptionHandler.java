package com.duoc.msresumenes.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import software.amazon.awssdk.core.exception.SdkException;

/** Traduce las excepciones a respuestas JSON con un formato único. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ ArchivoInvalidoException.class, SolicitudInvalidaException.class })
    public ResponseEntity<Map<String, Object>> invalido(RuntimeException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> faltaArchivo(MissingServletRequestPartException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "Falta el archivo: en Postman usa Body -> form-data, clave '" + ex.getRequestPartName() + "' de tipo File");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> archivoGrande(MaxUploadSizeExceededException ex) {
        return respuesta(HttpStatus.CONTENT_TOO_LARGE, "El archivo supera el tamaño máximo permitido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Formato inválido para el parámetro '" + ex.getName() + "'");
    }

    // ms-compras no responde (contenedor caído, URL mal configurada...)
    @ExceptionHandler(ServicioComprasException.class)
    public ResponseEntity<Map<String, Object>> comprasNoDisponible(ServicioComprasException ex) {
        log.error("Error al comunicarse con ms-compras", ex);
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // Errores de AWS: credenciales de AWS Academy vencidas, bucket inexistente, sin permisos...
    @ExceptionHandler({ io.awspring.cloud.s3.S3Exception.class, SdkException.class })
    public ResponseEntity<Map<String, Object>> errorS3(RuntimeException ex) {
        log.error("Error al comunicarse con AWS S3", ex);
        return respuesta(HttpStatus.BAD_GATEWAY,
                "No se pudo acceder a S3. Revisa que las credenciales de AWS Academy estén vigentes y que el bucket exista");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> general(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
            if (status != null) {
                return respuesta(status, status.getReasonPhrase());
            }
        }
        log.error("Error no controlado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
