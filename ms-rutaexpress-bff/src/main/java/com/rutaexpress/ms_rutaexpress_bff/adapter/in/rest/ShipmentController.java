package com.rutaexpress.ms_rutaexpress_bff.adapter.in.rest;

import com.rutaexpress.ms_rutaexpress_bff.adapter.in.rest.exception.ShipmentNotFoundException;
import com.rutaexpress.ms_rutaexpress_bff.port.out.DownstreamResponse;
import com.rutaexpress.ms_rutaexpress_bff.port.out.ShipmentView;
import com.rutaexpress.ms_rutaexpress_bff.port.out.ShipmentsPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

// Envíos para el front: aplica roles y reenvía a ms-rutaexpress-shipments
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentsPort shipments;
    private final ObjectMapper objectMapper;

    public ShipmentController(ShipmentsPort shipments, ObjectMapper objectMapper) {
        this.shipments = shipments;
        this.objectMapper = objectMapper;
    }

    // GET /api/v1/shipments?status=&from=&to=  (CLIENTE solo ve los suyos)
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR','ADMIN')")
    public ResponseEntity<String> list(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) String from,
                                       @RequestParam(required = false) String to,
                                       JwtAuthenticationToken auth) {
        DownstreamResponse response = shipments.list(status, from, to);
        if (!response.isSuccess() || !UserIdentity.isOnlyCustomer(auth)) {
            return passThrough(response);
        }
        String userId = UserIdentity.userId(auth.getToken());
        List<ShipmentView> own = Arrays.stream(objectMapper.readValue(response.body(), ShipmentView[].class))
                .filter(s -> userId.equals(s.customerId()))
                .toList();
        return json(200, objectMapper.writeValueAsString(own));
    }

    // GET /api/v1/shipments/{id}  (si es de otro cliente -> 404)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR','ADMIN')")
    public ResponseEntity<String> get(@PathVariable Long id, JwtAuthenticationToken auth) {
        DownstreamResponse response = shipments.get(id);
        if (response.isSuccess() && UserIdentity.isOnlyCustomer(auth)) {
            ShipmentView shipment = objectMapper.readValue(response.body(), ShipmentView.class);
            if (!UserIdentity.userId(auth.getToken()).equals(shipment.customerId())) {
                throw new ShipmentNotFoundException(id);
            }
        }
        return passThrough(response);
    }

    // POST /api/v1/shipments  (el dueño sale del token, nunca del body)
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR','ADMIN')")
    public ResponseEntity<String> create(@RequestBody String body, JwtAuthenticationToken auth) {
        return passThrough(shipments.create(body, UserIdentity.userId(auth.getToken())));
    }

    // PUT /api/v1/shipments/{id}/status  (solo despachador u admin)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<String> changeStatus(@PathVariable Long id, @RequestBody String body) {
        return passThrough(shipments.changeStatus(id, body));
    }

    // Devuelve el código y el cuerpo de shipments tal cual (200, 201, 400, 404, 409)
    private ResponseEntity<String> passThrough(DownstreamResponse response) {
        return json(response.status(), response.body());
    }

    private ResponseEntity<String> json(int status, String body) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
