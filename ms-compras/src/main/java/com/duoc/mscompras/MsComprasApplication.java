package com.duoc.mscompras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de COMPRAS.
 *
 * Responsabilidad: eventos y compras de entradas. Es el ÚNICO dueño de la base de
 * datos (AWS RDS MySQL). El microservicio de resúmenes no toca la BD: le pide los
 * datos de una compra a este servicio por HTTP (GET /api/compras/{numero}).
 */
@SpringBootApplication
public class MsComprasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsComprasApplication.class, args);
    }
}
