package com.duoc.mscompras.service;


import com.duoc.mscompras.dto.EventoRequest;
import com.duoc.mscompras.exception.EventoDuplicadoException;
import com.duoc.mscompras.model.Evento;

    @Transactional
    public EventoResponse crear(EventoRequest request) {
        String nombre = request.nombre().trim();
        if (eventoRepository.findByNombreIgnoreCase(nombre).isPresent()) {
            throw new EventoDuplicadoException("Ya existe un evento con el nombre '" + nombre + "'");
        }
        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setFecha(request.fecha());
        evento.setPrecio(request.precio());
        evento.setDisponible(true);
        return EventoResponse.desde(eventoRepository.save(evento));
    }
