package com.duoc.mscompras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.duoc.mscompras.dto.CompraRequest;
import com.duoc.mscompras.dto.CompraResponse;
import com.duoc.mscompras.exception.ApiException;
import com.duoc.mscompras.model.Compra;
import com.duoc.mscompras.model.Evento;
import com.duoc.mscompras.repository.CompraRepository;
import com.duoc.mscompras.repository.EventoRepository;
import com.duoc.mscompras.security.UsuarioActual;
import com.duoc.mscompras.service.CompraService;

// Pruebas UNITARIAS: sin Spring y sin base de datos. Los repositorios son "falsos" (mocks).
@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    private static final UsuarioActual ANA = new UsuarioActual("oid-ana", List.of("CLIENTE"));
    private static final UsuarioActual LUIS = new UsuarioActual("oid-luis", List.of("CLIENTE"));
    private static final UsuarioActual ADMIN = new UsuarioActual("oid-admin", List.of("ADMIN"));

    @Mock
    private CompraRepository compraRepository;     

    @Mock
    private EventoRepository eventoRepository;    

    @InjectMocks
    private CompraService compraService;            

    @Test
    void calculaElTotalConElPrecioDeLaBd() {
        when(eventoRepository.findByNombreIgnoreCase("evento test"))
                .thenReturn(Optional.of(evento(true)));
        // Simula el save: le pone id 1 y devuelve la misma compra
        when(compraRepository.save(any(Compra.class))).thenAnswer(inv -> {
            Compra c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CompraResponse compra = compraService.comprar(
                new CompraRequest("Ana Pérez", "ANA@Correo.cl ", " evento test ", 3), ANA);

        assertThat(compra.precioTotal()).isEqualByComparingTo("37500"); 
        assertThat(compra.emailCliente()).isEqualTo("ana@correo.cl");
        assertThat(compra.activa()).isTrue();
    }

    @Test
    void laCompraQuedaANombreDelUsuarioDelToken() {
        when(eventoRepository.findByNombreIgnoreCase("evento test"))
                .thenReturn(Optional.of(evento(true)));
        when(compraRepository.save(any(Compra.class))).thenAnswer(inv -> inv.getArgument(0));

        compraService.comprar(new CompraRequest("Ana", "ana@correo.cl", "evento test", 1), ANA);

        verify(compraRepository).save(org.mockito.ArgumentMatchers.argThat(
                c -> "oid-ana".equals(c.getUsuarioId())));
    }

    @Test
    void elDuenoPuedeVerSuCompra() {
        when(compraRepository.findConEventoById(7L)).thenReturn(Optional.of(compraDe("oid-ana", true)));
        CompraResponse leida = compraService.obtener(7L, ANA);
        assertThat(leida.numeroResumen()).isEqualTo(7L);
    }

    @Test
    void otroClienteNoPuedeVerUnaCompraAjena() {
        when(compraRepository.findConEventoById(7L)).thenReturn(Optional.of(compraDe("oid-ana", true)));

        assertThatThrownBy(() -> compraService.obtener(7L, LUIS))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);     
    }

    @Test
    void elAdminPuedeVerCualquierCompra() {
        when(compraRepository.findConEventoById(7L)).thenReturn(Optional.of(compraDe("oid-ana", true)));

        assertThat(compraService.obtener(7L, ADMIN).numeroResumen()).isEqualTo(7L);
    }

    @Test
    void compraDesactivadaNoLaVeElCliente() {
        when(compraRepository.findConEventoById(7L)).thenReturn(Optional.of(compraDe("oid-ana", false)));

        assertThatThrownBy(() -> compraService.obtener(7L, ANA))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);       
    }

    @Test
    void compraDesactivadaSiLaVeElAdmin() {
        when(compraRepository.findConEventoById(7L)).thenReturn(Optional.of(compraDe("oid-ana", false)));

        assertThat(compraService.obtener(7L, ADMIN).activa()).isFalse();
    }

    @Test
    void desactivarMarcaLaCompraYLaGuarda() {
        Compra compra = compraDe("oid-ana", true);
        when(compraRepository.findById(7L)).thenReturn(Optional.of(compra));

        compraService.desactivar(7L);

        assertThat(compra.isActiva()).isFalse();
        verify(compraRepository).save(compra);      // confirma si se llamo save
    }

    @Test
    void compraInexistenteResponde404() {
        when(compraRepository.findConEventoById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compraService.obtener(999L, ADMIN))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);       
    }

    @Test
    void eventoNoDisponibleNoSePuedeComprar() {
        when(eventoRepository.findByNombreIgnoreCase("evento test"))
                .thenReturn(Optional.of(evento(false)));

        assertThatThrownBy(() -> compraService.comprar(
                new CompraRequest("Ana", "ana@correo.cl", "evento test", 1), ANA))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);        

        verify(compraRepository, never()).save(any());   // no se guardó nada
    }

    // Datos de prueba

    private Evento evento(boolean disponible) {
        Evento evento = new Evento();
        evento.setId(1L);
        evento.setNombre("Evento Test");
        evento.setFecha(LocalDate.now().plusDays(10));
        evento.setPrecio(new BigDecimal("12500"));
        evento.setDisponible(disponible);
        return evento;
    }

    private Compra compraDe(String usuarioId, boolean activa) {
        Compra compra = new Compra();
        compra.setId(7L);
        compra.setNombreCliente("Ana Pérez");
        compra.setEmailCliente("ana@correo.cl");
        compra.setEvento(evento(true));
        compra.setCantidad(1);
        compra.setPrecioUnitario(new BigDecimal("12500"));
        compra.setPrecioTotal(new BigDecimal("12500"));
        compra.setFechaCompra(LocalDateTime.now());
        compra.setUsuarioId(usuarioId);
        compra.setActiva(activa);
        return compra;
    }
}