package com.duoc.mscompras.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.mscompras.dto.EventoRequest;
import com.duoc.mscompras.dto.EventoResponse;
import com.duoc.mscompras.exception.ApiException;
import com.duoc.mscompras.model.Evento;
import com.duoc.mscompras.repository.EventoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    // Lista los eventos (público)
    @Transactional(readOnly = true)
    public List<EventoResponse> listar() {
        return eventoRepository.findAll().stream()
                .map(EventoResponse::desde)
                .toList();
    }

    // Crea un evento nuevo (solo ADMIN o VENDEDOR, lo revisa el controller)
    @Transactional
    public EventoResponse crear(EventoRequest request) {
        String nombre = request.nombre().trim();
        if (eventoRepository.findByNombreIgnoreCase(nombre).isPresent()) {
            throw ApiException.conflicto("Ya existe un evento con el nombre '" + nombre + "'");   // 409
        }
        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setFecha(request.fecha());
        evento.setPrecio(request.precio());
        evento.setDisponible(true);
        return EventoResponse.desde(eventoRepository.save(evento));
    }
}