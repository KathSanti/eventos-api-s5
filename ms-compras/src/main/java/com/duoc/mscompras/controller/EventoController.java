package com.duoc.mscompras.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.mscompras.dto.EventoResponse;
import com.duoc.mscompras.service.EventoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    // Lista publica listado de eventos disponibles
    @GetMapping
    public List<EventoResponse> listar() {
        return eventoService.listar();
    }
}
