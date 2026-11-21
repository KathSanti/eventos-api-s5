package com.duoc.mscompras.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.mscompras.dto.CompraRequest;
import com.duoc.mscompras.dto.CompraResponse;
import com.duoc.mscompras.service.CompraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Controlador REST para comprar entradas y consultar compras
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    // Compra entradas Devuelve el numeroResumen.
    @PostMapping
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody CompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.comprar(request));
    }

    // Detalle de una compra (lo consume ms-resumenes para generar el resumen).
    @GetMapping("/{numero}")
    public CompraResponse obtener(@PathVariable("numero") Long numero) {
        return compraService.obtener(numero);
    }
}
