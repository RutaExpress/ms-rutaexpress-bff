package com.rutaexpress.ms_rutaexpress_bff.adapter.in.rest.exception;

// El envío no existe o no pertenece al cliente -> 404 (no se revela que existe)
public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(Long id) {
        super("Envío con ID " + id + " no fue encontrado.");
    }
}
