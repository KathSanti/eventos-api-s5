package com.duoc.msresumenes.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health check: lo usa GitHub Actions después de desplegar. */
@RestController
public class SaludController {

    @GetMapping("/salud")
    public Map<String, Object> salud() {
        return Map.of("estado", "OK", "servicio", "ms-resumenes");
    }
}
