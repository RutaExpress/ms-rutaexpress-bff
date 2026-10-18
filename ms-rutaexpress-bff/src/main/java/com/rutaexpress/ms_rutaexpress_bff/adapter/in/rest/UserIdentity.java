package com.rutaexpress.ms_rutaexpress_bff.adapter.in.rest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

// Utilidades de identidad a partir del JWT
final class UserIdentity {

    private UserIdentity() {}

    // oid = id único del usuario en Entra ID (si no viene, se usa sub)
    static String userId(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        return oid != null ? oid : jwt.getSubject();
    }

    static boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    // Solo CLIENTE (sin OPERADOR ni ADMIN): debe ver únicamente sus envíos
    static boolean isOnlyCustomer(Authentication auth) {
        return hasRole(auth, "ROLE_CLIENTE") && !hasRole(auth, "ROLE_OPERADOR") && !hasRole(auth, "ROLE_ADMIN");
    }
}
