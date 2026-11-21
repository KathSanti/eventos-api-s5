package com.duoc.msresumenes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de RESÚMENES DE COMPRA.
 *
 * Responsabilidad: los 5 endpoints que pide la actividad
 *   1) generar el resumen como archivo físico descargable,
 *   2) subirlo a S3 en la carpeta {numero}/,
 *   3) modificarlo, 4) descargarlo y 5) borrarlo de S3.
 *
 * Es el ÚNICO dueño del bucket S3. No tiene base de datos: los datos de la compra
 * se los pide a ms-compras por HTTP (ver client/ComprasClient).
 */
@SpringBootApplication
public class MsResumenesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsResumenesApplication.class, args);
    }
}
