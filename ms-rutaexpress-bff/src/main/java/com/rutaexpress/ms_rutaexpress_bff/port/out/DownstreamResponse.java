package com.rutaexpress.ms_rutaexpress_bff.port.out;

// Respuesta de un microservicio: código HTTP + cuerpo JSON tal cual
public record DownstreamResponse(int status, String body) {

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
}
