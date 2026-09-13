package com.rutaexpress.ms_rutaexpress_bff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
@RequestMapping("/api/v1")
public class TestController {

    // 1. Ruta Pública: Accesible para cualquiera sin iniciar sesión
    @GetMapping("/public/status")
    public Map<String, String> publicStatus() {
        return Map.of("mensaje", "El BFF de RutaExpress está en línea y funcionando libremente.");
    }

    // 2. Ruta de Cliente: Requiere token válido (Cliente por defecto, o roles superiores)
    @GetMapping("/clientes/dashboard")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('OPERADOR') or hasRole('ADMIN')")
    public Map<String, Object> clienteDashboard(Authentication authentication) {
        return Map.of(
            "mensaje", "¡Acceso concedido a la zona de Clientes!",
            "usuario_id", authentication.getName(),
            "roles_detectados", authentication.getAuthorities().toString()
        );
    }

    // 3. Ruta Estricta: EXCLUSIVA para Operadores
    @GetMapping("/operadores/panel")
    @PreAuthorize("hasRole('OPERADOR')")
    public Map<String, Object> operadorPanel(Authentication authentication) {
        return Map.of(
            "mensaje", "¡Acceso concedido! Bienvenido al Panel de Operadores.",
            "usuario_id", authentication.getName(),
            "roles_detectados", authentication.getAuthorities().toString()
        );
    }
}