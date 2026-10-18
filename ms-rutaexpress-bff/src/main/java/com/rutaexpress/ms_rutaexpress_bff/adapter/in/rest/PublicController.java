package com.rutaexpress.ms_rutaexpress_bff.adapter.in.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Ruta pública (sin token): la usa el API Gateway y el front para saber si el BFF está en línea
@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("servicio", "ms-rutaexpress-bff", "estado", "en línea");
    }
}
