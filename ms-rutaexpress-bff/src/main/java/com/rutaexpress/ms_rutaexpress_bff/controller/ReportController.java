package com.rutaexpress.ms_rutaexpress_bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rutaexpress.ms_rutaexpress_bff.client.ReportClient;

import lombok.RequiredArgsConstructor;

// reportería para el front (HU 8): solo el ADMIN ve los KPI de la red
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    // dependencia: el cliente de report
    private final ReportClient reportClient;

    // GET /kpis?range=last24h (indicadores): el rango por defecto es el del caso
    @GetMapping("/kpis")
    public ResponseEntity<String> kpis(@RequestParam(defaultValue = "last24h") String range) {
        return reportClient.kpis(range).toResponse();
    }

    // GET /top-services?range=last7d (servicios más usados)
    @GetMapping("/top-services")
    public ResponseEntity<String> topServices(@RequestParam(defaultValue = "last7d") String range) {
        return reportClient.topServices(range).toResponse();
    }
}
