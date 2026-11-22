package com.rutaexpress.ms_rutaexpress_bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;

// cliente HTTP hacia ms-rutaexpress-catalog (contrato 3: /api/v1/catalog/services)
@Component
public class CatalogClient {

    private static final String BASE_PATH = "/api/v1/catalog/services";

    private final RestClient restClient;

    public CatalogClient(@Value("${services.catalog-url}") String catalogUrl) {
        this.restClient = RestClient.builder().baseUrl(catalogUrl).build();
    }

    // list (listar servicios)
    public DownstreamResponse list() {
        return restClient.get()
                .uri(BASE_PATH)
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    // create (crear servicio): name, basePrice, currency, initialCapacity
    public DownstreamResponse create(String jsonBody) {
        return restClient.post()
                .uri(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    // update (editar tarifa o capacidad): basePrice, capacityDelta
    public DownstreamResponse update(String id, String jsonBody) {
        return restClient.put()
                .uri(BASE_PATH + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }
}
