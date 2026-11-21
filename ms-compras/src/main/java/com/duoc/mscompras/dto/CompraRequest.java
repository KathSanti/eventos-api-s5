package com.duoc.mscompras.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Datos de entrada para comprar
public record CompraRequest(
        @NotBlank @Size(max = 100) String nombreCliente,
        @NotBlank @Email @Size(max = 150) String emailCliente,
        @NotBlank String nombreEvento,
        @NotNull @Min(1) @Max(10) Integer cantidad
) {}
