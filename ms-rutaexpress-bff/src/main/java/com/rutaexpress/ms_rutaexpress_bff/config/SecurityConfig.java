package com.rutaexpress.ms_rutaexpress_bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar anotaciones de seguridad por método
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. HABILITAR CORS AQUÍ (Para que el Preflight de Angular pase sin problemas)
            .cors(Customizer.withDefaults()) 
            
            // Deshabilitar CSRF (No es necesario en APIs REST con tokens JWT)
            .csrf(csrf -> csrf.disable()) 
            
            // Configurar reglas de rutas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll() // Rutas públicas
                .anyRequest().authenticated() // Todo el resto del BFF requiere token
            )
            // Configurar como Servidor de Recursos OAuth2 con tu conversor de roles
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
            );
            
        return http.build();
    }

    // 2. CONFIGURACIÓN EXACTA DE CORS PARA ANGULAR
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Origen de tu frontend
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // Métodos permitidos, incluyendo OPTIONS crucial para el preflight
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cabeceras permitidas (Authorization lleva tu token Bearer)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta regla a todas tus rutas del backend
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1. Extraer el arreglo "roles" del token de Microsoft
            List<String> roles = jwt.getClaimAsStringList("roles");
            
            // 2. Si no hay roles en el token, asumimos que es un Cliente
            if (roles == null || roles.isEmpty()) {
                return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            }
            
            // 3. Si hay roles (ej. ROLE_OPERADOR, ROLE_ADMIN), los mapeamos a Spring Security
            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        
        return converter;
    }
}