package com.duoc.mscompras.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.mscompras.dto.CompraRequest;
import com.duoc.mscompras.dto.CompraResponse;
import com.duoc.mscompras.security.UsuarioActual;
import com.duoc.mscompras.service.CompraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Controlador REST para comprar entradas, consultarlas y desactivarlas
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;


    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody CompraRequest request,
                                                  @AuthenticationPrincipal Jwt jwt) {
        CompraResponse compra = compraService.comprar(request, UsuarioActual.desde(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(compra);   // 201
    }

    // Ver una comprael cliente solo las suyas y activas; el admin todas (lo revisa el service)
    @GetMapping("/{numero}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public CompraResponse obtener(@PathVariable("numero") Long numero,
                                  @AuthenticationPrincipal Jwt jwt) {
        return compraService.obtener(numero, UsuarioActual.desde(jwt));   // 200
    }

    // borrado lógico solo puede hacerlo el admin
    @DeleteMapping("/{numero}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable("numero") Long numero) {
        compraService.desactivar(numero);
        return ResponseEntity.noContent().build();   // 204
    }
}