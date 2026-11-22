package com.rutaexpress.ms_rutaexpress_bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rutaexpress.ms_rutaexpress_bff.client.ShipmentsClient;
import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;
import com.rutaexpress.ms_rutaexpress_bff.dto.ShipmentView;
import com.rutaexpress.ms_rutaexpress_bff.exception.ShipmentNotFoundException;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

// envíos para el front: aplica los roles (guía, matriz de la sección 3) y reenvía a ms-rutaexpress-shipments
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    // dependencias: el cliente de shipments y el conversor JSON, por el constructor que escribe Lombok
    private final ShipmentsClient shipmentsClient;
    private final ObjectMapper objectMapper;

    // GET /api/v1/shipments?status=&from=&to= (listar): el CLIENTE solo ve los suyos -> se manda su oid como customerId
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR','ADMIN')")
    public ResponseEntity<String> list(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) String from,
                                       @RequestParam(required = false) String to,
                                       JwtAuthenticationToken auth) {
        String customerId = UserIdentity.isOnlyCustomer(auth) ? UserIdentity.userId(auth.getToken()) : null;
        return shipmentsClient.list(status, from, to, customerId).toResponse();
    }

    // GET /api/v1/shipments/{id} (ver uno): si es de otro cliente -> 404 (no se revela que existe)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR','ADMIN')")
    public ResponseEntity<String> get(@PathVariable Long id, JwtAuthenticationToken auth) {
        DownstreamResponse response = shipmentsClient.get(id);
        if (response.isSuccess() && UserIdentity.isOnlyCustomer(auth)) {
            ShipmentView shipment = objectMapper.readValue(response.body(), ShipmentView.class);
            if (!UserIdentity.userId(auth.getToken()).equals(shipment.customerId())) {
                throw new ShipmentNotFoundException(id);
            }
        }
        return response.toResponse();
    }

    // POST /api/v1/shipments (crear): solo CLIENTE o despachador (caso, sección 6); el dueño sale del token, nunca del body
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    public ResponseEntity<String> create(@RequestBody String body, JwtAuthenticationToken auth) {
        return shipmentsClient.create(body, UserIdentity.userId(auth.getToken())).toResponse();
    }

    // PUT /api/v1/shipments/{id}/status (cambiar estado): despachador o admin; se reenvía quién lo hace (actor)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<String> changeStatus(@PathVariable Long id, @RequestBody String body, JwtAuthenticationToken auth) {
        return shipmentsClient.changeStatus(id, body, UserIdentity.userId(auth.getToken())).toResponse();
    }
}
