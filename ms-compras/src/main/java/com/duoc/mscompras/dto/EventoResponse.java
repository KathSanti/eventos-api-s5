package com.duoc.mscompras.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.duoc.mscompras.model.Evento;

// Catálogo de eventos sin id 
public record EventoResponse(
        String nombre,
        LocalDate fecha,
        BigDecimal precio,
        boolean disponible
) {

    public static EventoResponse desde(Evento evento) {
        return new EventoResponse(evento.getNombre(), evento.getFecha(), evento.getPrecio(), evento.isDisponible());
    }
}
