package com.rutaexpress.ms_rutaexpress_bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rutaexpress.ms_rutaexpress_bff.client.AuditClient;

import lombok.RequiredArgsConstructor;

// auditoría para el front (HU 9): timeline del envío, solo lectura, para AUDITOR y ADMIN
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    // dependencia: el cliente de audit
    private final AuditClient auditClient;

    // GET /shipments/{id} (timeline de un envío)
    @GetMapping("/shipments/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    public ResponseEntity<String> timeline(@PathVariable Long id) {
        return auditClient.timeline(id).toResponse();
    }
}
