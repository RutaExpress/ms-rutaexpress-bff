package com.rutaexpress.ms_rutaexpress_bff.dto;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

// respuesta de un microservicio (respuesta de aguas abajo): código HTTP + cuerpo JSON tal cual
public record DownstreamResponse(int status, String body) {

    // of (de): arma la respuesta con los bytes que llegaron por HTTP
    public static DownstreamResponse of(int status, byte[] body) {
        return new DownstreamResponse(status, new String(body, StandardCharsets.UTF_8));
    }

    // isSuccess (¿salió bien?): 2xx
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    // toResponse (a respuesta): devuelve al front el mismo código y cuerpo (200, 201, 400, 404, 409...)
    public ResponseEntity<String> toResponse() {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
