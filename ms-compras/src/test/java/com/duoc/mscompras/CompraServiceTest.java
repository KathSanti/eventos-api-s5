package com.duoc.mscompras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.duoc.mscompras.dto.CompraRequest;
import com.duoc.mscompras.dto.CompraResponse;
import com.duoc.mscompras.exception.EventoNoDisponibleException;
import com.duoc.mscompras.exception.RecursoNoEncontradoException;
import com.duoc.mscompras.model.Evento;
import com.duoc.mscompras.repository.EventoRepository;
import com.duoc.mscompras.service.CompraService;

//Pruebas de la lógica de compra contra una BD H2 en memoria (perfil test)
@SpringBootTest
@ActiveProfiles("test")
class CompraServiceTest {

    @Autowired
    private CompraService compraService;

    @Autowired
    private EventoRepository eventoRepository;

    @Test
    void calculaElTotalConElPrecioDeLaBd() {
        crearEvento("Evento Test Total", LocalDate.now().plusDays(10), "12500", true);

        CompraResponse compra = compraService.comprar(
                new CompraRequest("Ana Pérez", "ANA@Correo.cl ", "evento test total", 3));

        assertThat(compra.numeroResumen()).isNotNull();
        assertThat(compra.precioTotal()).isEqualByComparingTo("37500");
        assertThat(compra.emailCliente()).isEqualTo("ana@correo.cl");
    }

    @Test
    void obtenerDevuelveLaMismaCompra() {
        crearEvento("Evento Test Obtener", LocalDate.now().plusDays(5), "1000", true);
        CompraResponse creada = compraService.comprar(
                new CompraRequest("Luis Soto", "luis@correo.cl", "Evento Test Obtener", 1));

        CompraResponse leida = compraService.obtener(creada.numeroResumen());

        assertThat(leida.nombreEvento()).isEqualTo("Evento Test Obtener");
        assertThat(leida.nombreCliente()).isEqualTo("Luis Soto");
    }

    @Test
    void compraInexistenteResponde404() {
        assertThatThrownBy(() -> compraService.obtener(999_999L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eventoNoDisponibleNoSePuedeComprar() {
        crearEvento("Evento Test Cerrado", LocalDate.now().plusDays(5), "1000", false);

        assertThatThrownBy(() -> compraService.comprar(
                new CompraRequest("Ana", "ana@correo.cl", "Evento Test Cerrado", 1)))
                .isInstanceOf(EventoNoDisponibleException.class);
    }

    private void crearEvento(String nombre, LocalDate fecha, String precio, boolean disponible) {
        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setFecha(fecha);
        evento.setPrecio(new BigDecimal(precio));
        evento.setDisponible(disponible);
        eventoRepository.save(evento);
    }
}
