package com.rutaexpress.ms_rutaexpress_bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// levanta toda la aplicación para comprobar que cada pieza se puede crear e inyectar
@SpringBootTest
class MsRutaexpressBffApplicationTests {

	// decodificador de JWT simulado: sin esto, el arranque va a internet a buscar las llaves de Entra ID
	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void contextLoads() {
	}

}
