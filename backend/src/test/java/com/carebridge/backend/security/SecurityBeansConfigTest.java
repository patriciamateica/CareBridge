package com.carebridge.backend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityBeansConfigTest {

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    private final SecurityBeansConfig config = new SecurityBeansConfig();

    @Test
    void authenticationManagerBean_ShouldReturnAuthenticationManager() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = config.authenticationManagerBean(authenticationConfiguration);

        assertEquals(authenticationManager, result);
    }

    @Test
    void corsConfigurationSource_ShouldContainExpectedDefaults() {
        CorsConfigurationSource source = config.corsConfigurationSource();

        assertTrue(source instanceof UrlBasedCorsConfigurationSource);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/graphql");
        CorsConfiguration cors = source.getCorsConfiguration(request);

        assertNotNull(cors);
        assertEquals(true, cors.getAllowCredentials());
        assertEquals(1, cors.getAllowedOriginPatterns().size());
        assertEquals("*", cors.getAllowedOriginPatterns().get(0));
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowedMethods().contains("POST"));
    }

    @Test
    void passwordEncoder_ShouldEncodeAndMatch() {
        PasswordEncoder encoder = config.passwordEncoder();

        String encoded = encoder.encode("secret");

        assertNotNull(encoded);
        assertTrue(encoder.matches("secret", encoded));
    }
}

