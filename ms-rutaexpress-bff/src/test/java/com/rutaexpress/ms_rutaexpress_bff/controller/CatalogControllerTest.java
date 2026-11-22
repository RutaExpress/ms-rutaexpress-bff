package com.rutaexpress.ms_rutaexpress_bff.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rutaexpress.ms_rutaexpress_bff.client.CatalogClient;
import com.rutaexpress.ms_rutaexpress_bff.config.SecurityConfig;
import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;

// test de integración de la capa web del BFF para el catálogo (caso, sección 5; guía, matriz de la sección 3)
@WebMvcTest(CatalogController.class)
@Import(SecurityConfig.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // dependencia simulada (cliente de catalog)
    @MockitoBean
    private CatalogClient catalogClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // test 1 (regla principal): el OPERADOR (despachador) consulta el catálogo
    @Test
    @DisplayName("OPERADOR consulta servicios: 200")
    void operatorListsServices() throws Exception {
        // arrange (preparar)
        when(catalogClient.list()).thenReturn(new DownstreamResponse(200, "[]"));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/catalog/services")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERADOR"))))
                .andExpect(status().isOk());
        verify(catalogClient).list();
    }

    // test 2 (caso de error): el OPERADOR no crea servicios -> 403
    @Test
    @DisplayName("OPERADOR crea servicio: 403")
    void operatorCannotCreateService() throws Exception {
        mockMvc.perform(post("/api/v1/catalog/services")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERADOR")))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(catalogClient);
    }

    // test 3: el ADMIN edita tarifa o capacidad -> se reenvía a catalog con el id
    @Test
    @DisplayName("ADMIN edita servicio: reenvía id y body")
    void adminUpdatesService() throws Exception {
        // arrange (preparar)
        String body = "{\"capacityDelta\":5}";
        when(catalogClient.update("svc-1", body)).thenReturn(new DownstreamResponse(200, "{\"id\":\"svc-1\"}"));

        // act + assert (actuar y revisar)
        mockMvc.perform(put("/api/v1/catalog/services/svc-1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verify(catalogClient).update("svc-1", body);
    }
}
