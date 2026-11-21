package com.duoc.mscompras.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//Lo que se envía para crear un evento. 
public record EventoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotNull @Future LocalDate fecha,              // no se crean eventos en el pasado
        @NotNull @DecimalMin("1") BigDecimal precio    // precio mayor o igual a 1
) {}