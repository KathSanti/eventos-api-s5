package com.duoc.msresumenes.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.duoc.msresumenes.dto.CompraDto;

/** Arma el texto del resumen de compra (el mismo formato de la semana 1 y 2). */
@Component
public class GeneradorResumen {

    public static final String CONTENT_TYPE = "text/plain; charset=UTF-8";

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public String nombreArchivo(Long numero) {
        return "resumen-compra-" + numero + ".txt";
    }

    public byte[] generar(CompraDto compra) {
        String linea = "=".repeat(44);
        String texto = String.join("\n",
                linea,
                "        RESUMEN DE COMPRA N° " + compra.numeroResumen(),
                linea,
                "Fecha de compra : " + fechaHora(compra.fechaCompra()),
                "Cliente         : " + compra.nombreCliente(),
                "Email           : " + compra.emailCliente(),
                "",
                "Evento          : " + compra.nombreEvento(),
                "Fecha evento    : " + fecha(compra.fechaEvento()),
                "Cantidad        : " + compra.cantidad(),
                "Precio unitario : " + pesos(compra.precioUnitario()),
                "TOTAL           : " + pesos(compra.precioTotal()),
                linea,
                "");
        return texto.getBytes(StandardCharsets.UTF_8);
    }

    private String pesos(BigDecimal valor) {
        return valor == null ? "-" : NumberFormat.getCurrencyInstance(Locale.of("es", "CL")).format(valor);
    }

    private String fecha(String iso) {
        try {
            return LocalDate.parse(iso).format(FECHA);
        } catch (DateTimeParseException | NullPointerException e) {
            return String.valueOf(iso);
        }
    }

    private String fechaHora(String iso) {
        try {
            return LocalDateTime.parse(iso).format(FECHA_HORA);
        } catch (DateTimeParseException | NullPointerException e) {
            return String.valueOf(iso);
        }
    }
}
