package com.rutaexpress.ms_rutaexpress_bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rutaexpress.ms_rutaexpress_bff.client.CatalogClient;

import lombok.RequiredArgsConstructor;

// catálogo de servicios para el front (caso, sección 5): el ADMIN administra y el despachador consulta
@RestController
@RequestMapping("/api/v1/catalog/services")
@RequiredArgsConstructor
public class CatalogController {

    // dependencia: el cliente de catalog
    private final CatalogClient catalogClient;

    // GET (listar): ADMIN y OPERADOR
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<String> list() {
        return catalogClient.list().toResponse();
    }

    // POST (crear servicio): solo ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> create(@RequestBody String body) {
        return catalogClient.create(body).toResponse();
    }

    // PUT /{id} (editar tarifa o capacidad): solo ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        return catalogClient.update(id, body).toResponse();
    }
}
