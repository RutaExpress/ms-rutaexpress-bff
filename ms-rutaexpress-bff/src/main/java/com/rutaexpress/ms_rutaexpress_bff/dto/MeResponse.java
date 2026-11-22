package com.rutaexpress.ms_rutaexpress_bff.dto;

import java.util.List;

// Usuario autenticado: lo usa el front para armar el menú según el rol
public record MeResponse(String userId, String name, String email, List<String> roles) {}
