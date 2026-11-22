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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rutaexpress.ms_rutaexpress_bff.client.ShipmentsClient;
import com.rutaexpress.ms_rutaexpress_bff.config.SecurityConfig;
import com.rutaexpress.ms_rutaexpress_bff.dto.DownstreamResponse;

// test de integración de la capa web del BFF para envíos (controlador de envíos)
// @WebMvcTest levanta solo el controller, el manejador global de errores y la seguridad (@Import)
// el cliente HTTP hacia shipments es un mock: no hace falta tener shipments corriendo
@WebMvcTest(ShipmentController.class)
@Import(SecurityConfig.class)
class ShipmentControllerTest {

    // simulador de peticiones HTTP (MockMvc)
    @Autowired
    private MockMvc mockMvc;

    // dependencia simulada (cliente de shipments)
    @MockitoBean
    private ShipmentsClient shipmentsClient;

    // decodificador de JWT simulado: así Spring no va a internet a buscar las llaves de Entra ID
    @MockitoBean
    private JwtDecoder jwtDecoder;

    // datos: token simulado con un oid y un rol (claim roles -> autoridad)
    private JwtRequestPostProcessor user(String oid, String role) {
        return jwt().jwt(token -> token.claim("oid", oid)).authorities(new SimpleGrantedAuthority(role));
    }

    // test 1 (HU 3): el CLIENTE lista solo lo suyo -> el BFF manda su oid como customerId a shipments
    @Test
    @DisplayName("CLIENTE lista: manda su oid como customerId")
    void customerListSendsCustomerId() throws Exception {
        // arrange (preparar)
        when(shipmentsClient.list(null, null, null, "oid-cliente")).thenReturn(new DownstreamResponse(200, "[]"));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/shipments").with(user("oid-cliente", "ROLE_CLIENTE")))
                .andExpect(status().isOk());
        verify(shipmentsClient).list(null, null, null, "oid-cliente");
    }

    // test 2: el OPERADOR lista todos -> sin customerId, y los filtros pasan tal cual
    @Test
    @DisplayName("OPERADOR lista: sin customerId y con los filtros")
    void operatorListSendsNoCustomerId() throws Exception {
        // arrange (preparar)
        when(shipmentsClient.list("ACEPTADO", null, null, null)).thenReturn(new DownstreamResponse(200, "[]"));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/shipments").param("status", "ACEPTADO").with(user("oid-op", "ROLE_OPERADOR")))
                .andExpect(status().isOk());
        verify(shipmentsClient).list("ACEPTADO", null, null, null);
    }

    // test 3 (HU 2): el CLIENTE crea -> el dueño sale del token, nunca del body
    @Test
    @DisplayName("CLIENTE crea: reenvía el body con su oid")
    void customerCreatesWithOwnOid() throws Exception {
        // arrange (preparar)
        String body = "{\"serviceId\":\"svc-1\"}";
        when(shipmentsClient.create(body, "oid-cliente")).thenReturn(new DownstreamResponse(201, "{\"id\":1}"));

        // act + assert (actuar y revisar): el 201 de shipments pasa tal cual
        mockMvc.perform(post("/api/v1/shipments").with(user("oid-cliente", "ROLE_CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        verify(shipmentsClient).create(body, "oid-cliente");
    }

    // test 4 (caso de error, matriz de la guía): el ADMIN no crea envíos -> 403 y no se llama a shipments
    @Test
    @DisplayName("ADMIN crea: 403")
    void adminCannotCreate() throws Exception {
        mockMvc.perform(post("/api/v1/shipments").with(user("oid-admin", "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(shipmentsClient);
    }

    // test 5 (HU 7 y auditoría): cambiar estado reenvía el oid de quien lo hace (X-User-Id)
    @Test
    @DisplayName("OPERADOR cambia estado: reenvía su oid como actor")
    void changeStatusSendsActor() throws Exception {
        // arrange (preparar)
        String body = "{\"status\":\"ACEPTADO\"}";
        when(shipmentsClient.changeStatus(1L, body, "oid-op")).thenReturn(new DownstreamResponse(200, "{\"id\":1}"));

        // act + assert (actuar y revisar)
        mockMvc.perform(put("/api/v1/shipments/1/status").with(user("oid-op", "ROLE_OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verify(shipmentsClient).changeStatus(1L, body, "oid-op");
    }

    // test 6 (caso de error): un CLIENTE pide un envío ajeno -> 404 (no se revela que existe)
    @Test
    @DisplayName("CLIENTE ve un envío ajeno: 404")
    void customerSeesOthersShipmentGets404() throws Exception {
        // arrange (preparar): shipments responde un envío de otro cliente
        when(shipmentsClient.get(7L)).thenReturn(new DownstreamResponse(200, "{\"id\":7,\"customerId\":\"oid-otro\"}"));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/shipments/7").with(user("oid-cliente", "ROLE_CLIENTE")))
                .andExpect(status().isNotFound());
    }

    // test 7 (caso de error): sin token -> 401 y no se llama a shipments
    @Test
    @DisplayName("Sin token: 401")
    void withoutTokenGets401() throws Exception {
        mockMvc.perform(get("/api/v1/shipments"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(shipmentsClient);
    }
}
