package com.duoc.mscompras.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una compra de entradas. Su id es el NÚMERO DEL RESUMEN de compra
 * (y el nombre de la carpeta en el bucket S3).
 *
 * Antes la compra apuntaba a una entidad Cliente (usuario con contraseña de Spring Security).
 * Ahora los usuarios viven en Azure AD B2C, así que la compra guarda solo el nombre y el
 * email del comprador, que llegan en el cuerpo de la petición.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(name = "email_cliente", nullable = false, length = 150)
    private String emailCliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "precio_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;
}
