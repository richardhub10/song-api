package com.magat.song;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

class WebConfigCorsTest {

    @Test
    void corsConfigurationRegistersDeployedUiOrigin() {
        CorsConfiguration configuration = new WebConfig().buildCorsConfiguration(
                "http://localhost:5173,https://song-ui-hfg8.onrender.com"
        );

        assertTrue(configuration.getAllowedOrigins().contains("https://song-ui-hfg8.onrender.com"));
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
    }
}