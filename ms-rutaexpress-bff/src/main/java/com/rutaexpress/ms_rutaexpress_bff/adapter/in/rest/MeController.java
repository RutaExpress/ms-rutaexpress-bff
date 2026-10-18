package com.rutaexpress.ms_rutaexpress_bff.adapter.in.rest;

import com.rutaexpress.ms_rutaexpress_bff.adapter.in.rest.dto.MeResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// GET /api/v1/me: datos del usuario sacados del JWT (cualquier usuario autenticado)
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    @GetMapping
    public MeResponse me(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        String email = jwt.getClaimAsString("preferred_username") != null
                ? jwt.getClaimAsString("preferred_username")
                : jwt.getClaimAsString("email");
        return new MeResponse(
                UserIdentity.userId(jwt),
                jwt.getClaimAsString("name"),
                email,
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}
