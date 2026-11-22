package com.rutaexpress.ms_rutaexpress_bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;

// cliente HTTP hacia ms-rutaexpress-audit (timeline del envío, solo lectura)
@Component
public class AuditClient {

    private static final String BASE_PATH = "/api/v1/audit";

    private final RestClient restClient;

    public AuditClient(@Value("${services.audit-url}") String auditUrl) {
        this.restClient = RestClient.builder().baseUrl(auditUrl).build();
    }

    // timeline (línea de tiempo) de un envío: quién, qué, cuándo y desde dónde
    public DownstreamResponse timeline(Long shipmentId) {
        return restClient.get()
                .uri(BASE_PATH + "/shipments/{id}", shipmentId)
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }
}
