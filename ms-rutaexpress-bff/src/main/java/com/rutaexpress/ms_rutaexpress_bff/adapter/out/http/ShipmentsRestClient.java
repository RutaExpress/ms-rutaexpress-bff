package com.rutaexpress.ms_rutaexpress_bff.adapter.out.http;

import com.rutaexpress.ms_rutaexpress_bff.port.out.DownstreamResponse;
import com.rutaexpress.ms_rutaexpress_bff.port.out.ShipmentsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

// Adaptador HTTP hacia ms-rutaexpress-shipments. Devuelve el código y el cuerpo tal cual (400/404/409 incluidos)
@Component
public class ShipmentsRestClient implements ShipmentsPort {

    private static final String BASE_PATH = "/api/v1/shipments";
    private final RestClient restClient;

    public ShipmentsRestClient(@Value("${services.shipments-url}") String shipmentsUrl) {
        this.restClient = RestClient.builder().baseUrl(shipmentsUrl).build();
    }

    @Override
    public DownstreamResponse list(String status, String from, String to) {
        return restClient.get()
                .uri(b -> b.path(BASE_PATH)
                        .queryParamIfPresent("status", Optional.ofNullable(status))
                        .queryParamIfPresent("from", Optional.ofNullable(from))
                        .queryParamIfPresent("to", Optional.ofNullable(to))
                        .build())
                .exchange((request, response) -> toDownstream(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    @Override
    public DownstreamResponse get(Long id) {
        return restClient.get()
                .uri(BASE_PATH + "/{id}", id)
                .exchange((request, response) -> toDownstream(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    @Override
    public DownstreamResponse create(String jsonBody, String userId) {
        return restClient.post()
                .uri(BASE_PATH)
                .header("X-User-Id", userId)               // identidad del usuario (claim oid del JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .exchange((request, response) -> toDownstream(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    @Override
    public DownstreamResponse changeStatus(Long id, String jsonBody) {
        return restClient.put()
                .uri(BASE_PATH + "/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .exchange((request, response) -> toDownstream(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    private DownstreamResponse toDownstream(int status, byte[] body) {
        return new DownstreamResponse(status, new String(body, StandardCharsets.UTF_8));
    }
}
