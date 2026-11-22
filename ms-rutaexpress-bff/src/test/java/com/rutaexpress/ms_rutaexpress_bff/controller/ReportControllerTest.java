package com.rutaexpress.ms_rutaexpress_bff.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import com.rutaexpress.ms_rutaexpress_bff.client.ReportClient;
import com.rutaexpress.ms_rutaexpress_bff.config.SecurityConfig;
import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;

// test de integración de la capa web del BFF para reportería (HU 8; caso, sección 5: /kpis y /top-services)
@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // dependencia simulada (cliente de report)
    @MockitoBean
    private ReportClient reportClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // test 1 (regla principal): el ADMIN pide los KPI -> el rango pasa a report (por defecto last24h)
    @Test
    @DisplayName("ADMIN pide KPI: 200 con el rango por defecto")
    void adminGetsKpis() throws Exception {
        // arrange (preparar)
        when(reportClient.kpis("last24h")).thenReturn(new DownstreamResponse(200, "{}"));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/report/kpis").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
        verify(reportClient).kpis("last24h");
    }

    // test 2 (caso de error): el OPERADOR no ve reportería -> 403
    @Test
    @DisplayName("OPERADOR pide KPI: 403")
    void operatorCannotGetKpis() throws Exception {
        mockMvc.perform(get("/api/v1/report/kpis").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERADOR"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(reportClient);
    }

    // test 3 (caso de error, contrato 5): report no responde -> 503 con ProblemDetail
    @Test
    @DisplayName("report caído: 503")
    void reportDownGets503() throws Exception {
        // arrange (preparar): el cliente no logra conectarse
        when(reportClient.topServices("last7d")).thenThrow(new ResourceAccessException("Connection refused"));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/report/top-services").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Servicio no disponible"));
    }
}
