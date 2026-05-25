package com.ikeu.components.security.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenPairTest {

    @Test
    void builder_allFields() {
        TokenPair pair = TokenPair.builder()
                .accessToken("access-xxx")
                .refreshToken("refresh-xxx")
                .expiresIn(7200)
                .refreshExpiresIn(604800)
                .build();
        assertEquals("access-xxx", pair.getAccessToken());
        assertEquals("refresh-xxx", pair.getRefreshToken());
        assertEquals(7200, pair.getExpiresIn());
        assertEquals(604800, pair.getRefreshExpiresIn());
    }

    @Test
    void builder_singleMode_nullRefresh() {
        TokenPair pair = TokenPair.builder()
                .accessToken("access-xxx")
                .expiresIn(3600)
                .build();
        assertNotNull(pair.getAccessToken());
        assertNull(pair.getRefreshToken());
        assertEquals(0, pair.getRefreshExpiresIn());
    }

    @Test
    void noArgsConstructor_setters() {
        TokenPair pair = new TokenPair();
        pair.setAccessToken("at");
        pair.setRefreshToken("rt");
        pair.setExpiresIn(100);
        pair.setRefreshExpiresIn(200);
        assertEquals("at", pair.getAccessToken());
        assertEquals("rt", pair.getRefreshToken());
        assertEquals(100, pair.getExpiresIn());
        assertEquals(200, pair.getRefreshExpiresIn());
    }

    @Test
    void allArgsConstructor() {
        TokenPair pair = new TokenPair("at", "rt", 300, 400);
        assertEquals("at", pair.getAccessToken());
        assertEquals("rt", pair.getRefreshToken());
        assertEquals(300, pair.getExpiresIn());
        assertEquals(400, pair.getRefreshExpiresIn());
    }
}