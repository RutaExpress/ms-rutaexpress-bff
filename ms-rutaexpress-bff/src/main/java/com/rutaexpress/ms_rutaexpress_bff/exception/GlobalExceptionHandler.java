package com.rutaexpress.ms_rutaexpress_bff.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import lombok.extern.slf4j.Slf4j;

// errores propios del BFF (los 400/404/409 de los microservicios se devuelven tal cual desde el controller)
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: el envío no existe o no es del cliente que lo pide
    @ExceptionHandler(ShipmentNotFoundException.class)
    public ProblemDetail handleNotFound(ShipmentNotFoundException ex) {
        log.warn("[HTTP 404] {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Envío no encontrado");
        return pd;
    }

    // 503: un microservicio (shipments, catalog, report o audit) está apagado o no se alcanza (contrato 5)
    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handleServiceDown(ResourceAccessException ex) {
        log.error("[HTTP 503] servicio no disponible: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Un servicio de RutaExpress no está disponible. Intente más tarde.");
        pd.setTitle("Servicio no disponible");
        return pd;
    }
}
