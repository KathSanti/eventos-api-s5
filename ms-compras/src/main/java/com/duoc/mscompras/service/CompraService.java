package com.duoc.mscompras.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.mscompras.dto.CompraRequest;
import com.duoc.mscompras.dto.CompraResponse;
import com.duoc.mscompras.exception.EventoNoDisponibleException;
import com.duoc.mscompras.exception.RecursoNoEncontradoException;
import com.duoc.mscompras.model.Compra;
import com.duoc.mscompras.model.Evento;
import com.duoc.mscompras.repository.CompraRepository;
import com.duoc.mscompras.repository.EventoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final EventoRepository eventoRepository;

    @Transactional
    public CompraResponse comprar(CompraRequest request) {
        String nombre = request.nombreEvento().trim();
        Evento evento = eventoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un evento con el nombre '" + nombre + "'"));

        if (!evento.isDisponible() || evento.getFecha().isBefore(LocalDate.now())) {
            throw new EventoNoDisponibleException("El evento '" + evento.getNombre() + "' no está disponible para la venta");
        }

        
        BigDecimal precioUnitario = evento.getPrecio();
        BigDecimal precioTotal = precioUnitario.multiply(BigDecimal.valueOf(request.cantidad()));

        Compra compra = new Compra();
        compra.setNombreCliente(request.nombreCliente().trim());
        compra.setEmailCliente(request.emailCliente().trim().toLowerCase(Locale.ROOT));
        compra.setEvento(evento);
        compra.setCantidad(request.cantidad());
        compra.setPrecioUnitario(precioUnitario);
        compra.setPrecioTotal(precioTotal);
        compra.setFechaCompra(LocalDateTime.now());

        return CompraResponse.desde(compraRepository.save(compra));
    }

    // Lo usa ms-resumenes para armar el resumen de compra
    @Transactional(readOnly = true)
    public CompraResponse obtener(Long numero) {
        return compraRepository.findConEventoById(numero)
                .map(CompraResponse::desde) 
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la compra N° " + numero));
    }
}
