package com.duoc.msresumenes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.duoc.msresumenes.client.ComprasClient;
import com.duoc.msresumenes.dto.ArchivoResumen;
import com.duoc.msresumenes.dto.CompraDto;
import com.duoc.msresumenes.exception.ArchivoInvalidoException;
import com.duoc.msresumenes.exception.RecursoNoEncontradoException;
import com.duoc.msresumenes.exception.SolicitudInvalidaException;
import com.duoc.msresumenes.model.Asset;
import com.duoc.msresumenes.repository.S3Repository;
import com.duoc.msresumenes.service.GeneradorResumen;
import com.duoc.msresumenes.service.ResumenService;

/**
 * Pruebas unitarias SIN AWS y SIN ms-compras: se reemplazan por dobles de prueba
 * (un ms-compras falso y un "bucket" en memoria). Por eso ComprasClient y S3Repository son interfaces.
 */
class ResumenServiceTest {

    private S3EnMemoria s3;
    private ResumenService servicio;

    @BeforeEach
    void preparar() {
        s3 = new S3EnMemoria();
        servicio = new ResumenService(new ComprasFalso(), s3, new GeneradorResumen());
    }

    @Test
    void generaElArchivoConLosDatosDeLaCompra() {
        ArchivoResumen archivo = servicio.generarArchivo(15L);

        String texto = new String(archivo.contenido(), StandardCharsets.UTF_8);
        assertThat(archivo.nombreArchivo()).isEqualTo("resumen-compra-15.txt");
        assertThat(texto).contains("RESUMEN DE COMPRA N° 15", "Ana Pérez", "Festival de Jazz", "20-03-2027");
    }

    @Test
    void subeAS3EnLaCarpetaDelNumero() {
        Asset asset = servicio.subirAS3(15L);

        assertThat(asset.getKey()).isEqualTo("15/resumen-compra-15.txt");
        assertThat(asset.getCarpeta()).isEqualTo("15/");
        assertThat(s3.existe("15/resumen-compra-15.txt")).isTrue();
    }

    @Test
    void modificarReemplazaElContenido() {
        servicio.subirAS3(15L);
        MockMultipartFile corregido = new MockMultipartFile(
                "file", "resumen-corregido.txt", "text/plain", "contenido corregido".getBytes(StandardCharsets.UTF_8));

        servicio.modificarEnS3(15L, corregido);

        String descargado = new String(servicio.descargarDeS3(15L).contenido(), StandardCharsets.UTF_8);
        assertThat(descargado).isEqualTo("contenido corregido");
    }

    @Test
    void modificarSinHaberSubidoResponde404() {
        MockMultipartFile archivo = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());
        assertThatThrownBy(() -> servicio.modificarEnS3(15L, archivo))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void modificarConUnArchivoQueNoEsTxtResponde400() {
        servicio.subirAS3(15L);
        MockMultipartFile pdf = new MockMultipartFile("file", "a.pdf", "application/pdf", "x".getBytes());
        assertThatThrownBy(() -> servicio.modificarEnS3(15L, pdf))
                .isInstanceOf(ArchivoInvalidoException.class);
    }

    @Test
    void despuesDeBorrarYaNoSePuedeDescargar() {
        servicio.subirAS3(15L);

        servicio.eliminarDeS3(15L);

        assertThatThrownBy(() -> servicio.descargarDeS3(15L)).isInstanceOf(RecursoNoEncontradoException.class);
        assertThatThrownBy(() -> servicio.eliminarDeS3(15L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void compraInexistenteResponde404() {
        assertThatThrownBy(() -> servicio.subirAS3(99L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void numeroMenorQueUnoResponde400() {
        assertThatThrownBy(() -> servicio.generarArchivo(0L)).isInstanceOf(SolicitudInvalidaException.class);
    }

    // ---------------- Dobles de prueba ----------------

    /** ms-compras falso: solo existe la compra N° 15. */
    static class ComprasFalso implements ComprasClient {
        @Override
        public CompraDto obtenerCompra(Long numero) {
            if (numero != 15L) {
                throw new RecursoNoEncontradoException("No existe la compra N° " + numero);
            }
            return new CompraDto(15L, "Ana Pérez", "ana@correo.cl", "Festival de Jazz", "2027-03-20",
                    2, new BigDecimal("18000"), new BigDecimal("36000"), "2026-11-12T10:30:00");
        }
    }

    /** "Bucket" en memoria: un Map clave -> contenido. */
    static class S3EnMemoria implements S3Repository {
        private final Map<String, byte[]> archivos = new HashMap<>();

        @Override
        public Asset subir(String key, byte[] contenido, String contentType) {
            archivos.put(key, contenido);
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
            return archivos.get(key);
        }

        @Override
        public boolean existe(String key) {
            return archivos.containsKey(key);
        }

        @Override
        public void eliminar(String key) {
            archivos.remove(key);
        }
    }
}
