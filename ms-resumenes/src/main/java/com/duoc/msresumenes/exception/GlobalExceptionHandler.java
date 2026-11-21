package com.duoc.msresumenes.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import software.amazon.awssdk.core.exception.SdkException;

// Traduce las excepciones a respuestas JSON 
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  

    // No existe (404), archivo o número inválido (400), compra ajena (403), ms-compras caído (503)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> errorDeNegocio(ApiException ex) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("Error al comunicarse con ms-compras", ex);   // los 503 quedan en el log
        }
        return respuesta(ex.getStatus(), ex.getMessage());
    }

    //Error spring 

    // Sin rol
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> sinRol(AccessDeniedException ex) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes el rol necesario para esta acción");
    }

    // sin formato form-data
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> faltaArchivo(MissingServletRequestPartException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "Falta el archivo: en Postman usa Body -> form-data, clave '" + ex.getRequestPartName() + "' de tipo File");
    }

    // Archivo más grande que el máximo permitido
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> archivoGrande(MaxUploadSizeExceededException ex) {
        return respuesta(HttpStatus.CONTENT_TOO_LARGE, "El archivo supera el tamaño máximo permitido");
    }

    // Parámetro con formato malo
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Formato inválido para el parámetro '" + ex.getName() + "'");
    }

    // Errores AWS

    // Credenciales de AWS Academy vencidas, bucket inexistente, sin permisos...
    @ExceptionHandler({ io.awspring.cloud.s3.S3Exception.class, SdkException.class })
    public ResponseEntity<Map<String, Object>> errorS3(RuntimeException ex) {
        log.error("Error al comunicarse con AWS S3", ex);
        return respuesta(HttpStatus.BAD_GATEWAY,
                "No se pudo acceder a S3. Revisa que las credenciales de AWS Academy estén vigentes y que el bucket exista");
    }

    // Otros errores 

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

    // JSON de respuesta para todos los errores 
    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}