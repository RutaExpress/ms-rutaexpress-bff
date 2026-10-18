package com.rutaexpress.ms_rutaexpress_bff.adapter.in.rest.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

// Errores propios del BFF (los 400/404/409 de shipments se devuelven tal cual desde el controller)
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: no existe o no es del cliente
    @ExceptionHandler(ShipmentNotFoundException.class)
    public ProblemDetail handleNotFound(ShipmentNotFoundException ex) {
        log.warn("[HTTP 404] {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Envío no encontrado");
        return pd;
    }

    // 503: shipments apagado o inalcanzable
    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handleServiceDown(ResourceAccessException ex) {
        log.error("[HTTP 503] shipments no disponible: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de envíos no está disponible. Intente más tarde.");
        pd.setTitle("Servicio no disponible");
        return pd;
    }
}
