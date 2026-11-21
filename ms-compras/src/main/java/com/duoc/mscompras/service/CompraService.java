package com.duoc.mscompras.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.mscompras.dto.CompraRequest;
import com.duoc.mscompras.dto.CompraResponse;
import com.duoc.mscompras.exception.ApiException;
import com.duoc.mscompras.model.Compra;
import com.duoc.mscompras.model.Evento;
import com.duoc.mscompras.repository.CompraRepository;
import com.duoc.mscompras.repository.EventoRepository;
import com.duoc.mscompras.security.UsuarioActual;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final EventoRepository eventoRepository;

    // la compra queda a nombre de quien viene en el token
    @Transactional
    public CompraResponse comprar(CompraRequest request, UsuarioActual usuario) {
        String nombre = request.nombreEvento().trim();
        Evento evento = eventoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> ApiException.noEncontrado("No existe un evento con el nombre '" + nombre + "'"));

        if (!evento.isDisponible() || evento.getFecha().isBefore(LocalDate.now())) {
            throw ApiException.conflicto("El evento '" + evento.getNombre() + "' no está disponible para la venta");
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
        compra.setUsuarioId(usuario.id());   
        compra.setActiva(true);

        return CompraResponse.desde(compraRepository.save(compra));
    }

    // El cliente solo ve su compras activas El admin ve todas
    // También lo usa ms-resumenes, reenviando el token del usuario
    @Transactional(readOnly = true)
    public CompraResponse obtener(Long numero, UsuarioActual usuario) {
        Compra compra = compraRepository.findConEventoById(numero)
                .orElseThrow(() -> noExiste(numero));

        if (usuario.esAdmin()) {
            return CompraResponse.desde(compra);
        }
        if (!compra.isActiva()) {
            throw noExiste(numero);          // desactivada: para el cliente "ya no existe"
        }
        if (!usuario.id().equals(compra.getUsuarioId())) {
            throw ApiException.prohibido("La compra N° " + numero + " no te pertenece");
        }
        return CompraResponse.desde(compra);
    }

    // Borrado lógico: no se elimina de la BD, solo se marca como desactivada (solo ADMIN)
    @Transactional
    public void desactivar(Long numero) {
        Compra compra = compraRepository.findById(numero)
                .orElseThrow(() -> noExiste(numero));
        compra.setActiva(false);
        compraRepository.save(compra);
    }

    // Mismo mensaje en los dos lugares donde la compra "no existe"
    private ApiException noExiste(Long numero) {
        return ApiException.noEncontrado("No existe la compra N° " + numero);
    }
}