package com.rutaexpress.ms_rutaexpress_bff.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.rutaexpress.ms_rutaexpress_bff.client.AuditClient;
import com.rutaexpress.ms_rutaexpress_bff.config.SecurityConfig;
import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;

// test de integración de la capa web del BFF para auditoría (HU 9; caso: Auditor, solo lectura)
@WebMvcTest(AuditController.class)
@Import(SecurityConfig.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // dependencia simulada (cliente de audit)
    @MockitoBean
    private AuditClient auditClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // test 1 (regla principal): el AUDITOR consulta el timeline de un envío
    @Test
    @DisplayName("AUDITOR consulta timeline: 200")
    void auditorGetsTimeline() throws Exception {
        // arrange (preparar)
        when(auditClient.timeline(15L)).thenReturn(new DownstreamResponse(200, "[]"));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/audit/shipments/15").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUDITOR"))))
                .andExpect(status().isOk());
        verify(auditClient).timeline(15L);
    }

    // test 2 (caso de error): el CLIENTE no ve auditoría -> 403
    @Test
    @DisplayName("CLIENTE consulta timeline: 403")
    void customerCannotGetTimeline() throws Exception {
        mockMvc.perform(get("/api/v1/audit/shipments/15").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(auditClient);
    }
}
