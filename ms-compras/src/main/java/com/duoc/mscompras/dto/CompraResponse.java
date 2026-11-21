package com.duoc.mscompras.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.duoc.mscompras.model.Compra;
import com.fasterxml.jackson.annotation.JsonFormat;

// Datos de salida de una compra
public record CompraResponse(
        Long numeroResumen,
        String nombreCliente,
        String emailCliente,
        String nombreEvento,
        @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate fechaEvento,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal precioTotal,
        @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime fechaCompra
) {

    public static CompraResponse desde(Compra compra) {
        return new CompraResponse(
                compra.getId(),
                compra.getNombreCliente(),
                compra.getEmailCliente(),
                compra.getEvento().getNombre(),
                compra.getEvento().getFecha(),
                compra.getCantidad(),
                compra.getPrecioUnitario(),
                compra.getPrecioTotal(),
                compra.getFechaCompra());
    }
}
