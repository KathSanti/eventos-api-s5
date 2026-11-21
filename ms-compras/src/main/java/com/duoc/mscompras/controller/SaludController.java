package com.duoc.mscompras.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// este edpoint es consumido por GitHub Actions después de desplegar
@RestController
public class SaludController {

    @GetMapping("/salud")
    public Map<String, Object> salud() {
        return Map.of("estado", "OK", "servicio", "ms-compras");
    }
}
