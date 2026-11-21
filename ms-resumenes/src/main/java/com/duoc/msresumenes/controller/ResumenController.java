package com.duoc.msresumenes.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.duoc.msresumenes.dto.ArchivoResumen;
import com.duoc.msresumenes.model.Asset;
import com.duoc.msresumenes.service.ResumenService;

/**
 * Los 5 endpoints que pide la actividad. Todos se inscriben en AWS API Gateway
 * y se protegen con el token de Azure AD B2C (el backend ya no valida JWT).
 *
 *  GET    /api/resumenes/{numero}      -> generar el resumen como archivo físico
 *  POST   /api/resumenes/{numero}/s3   -> subirlo a S3 (carpeta {numero}/)
 *  PUT    /api/resumenes/{numero}/s3   -> modificarlo (form-data, campo "file")
 *  GET    /api/resumenes/{numero}/s3   -> descargarlo desde S3
 *  DELETE /api/resumenes/{numero}/s3   -> borrarlo de S3
 */
@RestController
@RequestMapping("/api/resumenes/{numero}")
public class ResumenController {

    private final ResumenService resumenService;

    public ResumenController(ResumenService resumenService) {
        this.resumenService = resumenService;
    }

    @GetMapping
    public ResponseEntity<byte[]> generar(@PathVariable("numero") Long numero) {
        return comoDescarga(resumenService.generarArchivo(numero));
    }

    @PostMapping("/s3")
    public ResponseEntity<Asset> subir(@PathVariable("numero") Long numero) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumenService.subirAS3(numero));
    }

    @PutMapping(value = "/s3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Asset> modificar(@PathVariable("numero") Long numero,
                                           @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(resumenService.modificarEnS3(numero, file));
    }

    @GetMapping("/s3")
    public ResponseEntity<byte[]> descargar(@PathVariable("numero") Long numero) {
        return comoDescarga(resumenService.descargarDeS3(numero));
    }

    @DeleteMapping("/s3")
    public ResponseEntity<Void> eliminar(@PathVariable("numero") Long numero) {
        resumenService.eliminarDeS3(numero);
        return ResponseEntity.noContent().build();
    }

    /** "Content-Disposition: attachment" hace que el navegador/Postman lo guarde como archivo. */
    private ResponseEntity<byte[]> comoDescarga(ArchivoResumen archivo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombreArchivo()).build().toString())
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .contentLength(archivo.contenido().length)
                .body(archivo.contenido());
    }
}
