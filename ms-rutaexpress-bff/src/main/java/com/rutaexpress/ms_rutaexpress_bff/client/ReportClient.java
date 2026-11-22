package com.rutaexpress.ms_rutaexpress_bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;

// cliente HTTP hacia ms-rutaexpress-report (caso, sección 5: /kpis y /top-services, solo lectura)
@Component
public class ReportClient {

    private static final String BASE_PATH = "/api/v1/report";

    private final RestClient restClient;

    public ReportClient(@Value("${services.report-url}") String reportUrl) {
        this.restClient = RestClient.builder().baseUrl(reportUrl).build();
    }

    // kpis: envíos por hora, lead time y estados activos en el rango (last24h, last7d)
    public DownstreamResponse kpis(String range) {
        return restClient.get()
                .uri(b -> b.path(BASE_PATH + "/kpis").queryParam("range", range).build())
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }

    // topServices (servicios más usados)
    public DownstreamResponse topServices(String range) {
        return restClient.get()
                .uri(b -> b.path(BASE_PATH + "/top-services").queryParam("range", range).build())
                .exchange((request, response) -> DownstreamResponse.of(response.getStatusCode().value(), response.getBody().readAllBytes()));
    }
}
