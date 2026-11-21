package com.duoc.msresumenes.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Copia de la respuesta de ms-compras (GET /api/compras/{numero}).
 *
 * En microservicios cada servicio tiene SU PROPIA clase para los datos que recibe:
 * no se comparte código ni base de datos, solo el "contrato" JSON.
 * Las fechas se reciben como texto ISO (2027-03-20 / 2026-11-12T10:30:00) y se formatean al generar el resumen.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CompraDto(
        Long numeroResumen,
        String nombreCliente,
        String emailCliente,
        String nombreEvento,
        String fechaEvento,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal precioTotal,
        String fechaCompra
) {}
