package com.rutaexpress.ms_rutaexpress_bff.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;

// cliente HTTP hacia ms-rutaexpress-shipments
// devuelve el código y el cuerpo tal cual (400/404/409 incluidos)
@Component
public class ShipmentsClient {

    private static final String BASE_PATH = "/api/v1/shipments";

    // RestClient (cliente REST) con la URL de shipments (SHIPMENTS_URL)
    private final RestClient restClient;

    public ShipmentsClient(@Value("${services.shipments-url}") String shipmentsUrl) {
        this.restClient = RestClient.builder().baseUrl(shipmentsUrl).build();
    }

    // list (listar): customerId solo viene cuando quien pide es un CLIENTE (ve solo lo suyo)
    public DownstreamResponse list(String status, String from, String to, String customerId) {
        return restClient.get()
                .uri(b -> b.path(BASE_PATH)
                        .queryParamIfPresent("status", Optional.ofNullable(status))
                        .queryParamIfPresent("from", Optional.ofNullable(from))
                        .queryParamIfPresent("to", Optional.ofNullable(to))
                        .queryParamIfPresent("customerId", Optional.ofNullable(customerId))
                        .build())
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    // get (ver uno)
    public DownstreamResponse get(Long id) {
        return restClient.get()
                .uri(BASE_PATH + "/{id}", id)
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    // create (crear): X-User-Id es el dueño del envío (claim oid del token)
    public DownstreamResponse create(String jsonBody, String userId) {
        return restClient.post()
                .uri(BASE_PATH)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    // changeStatus (cambiar estado): X-User-Id es quien hace el cambio; shipments lo publica como actor en audit.timeline
    public DownstreamResponse changeStatus(Long id, String jsonBody, String userId) {
        return restClient.put()
                .uri(BASE_PATH + "/{id}/status", id)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }
}
