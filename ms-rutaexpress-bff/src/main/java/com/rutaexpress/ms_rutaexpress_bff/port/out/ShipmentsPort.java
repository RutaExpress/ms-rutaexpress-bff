package com.rutaexpress.ms_rutaexpress_bff.port.out;

// Puerto de salida: lo que el BFF necesita del microservicio de envíos
public interface ShipmentsPort {

    DownstreamResponse list(String status, String from, String to);

    DownstreamResponse get(Long id);

    DownstreamResponse create(String jsonBody, String userId);

    DownstreamResponse changeStatus(Long id, String jsonBody);
}
