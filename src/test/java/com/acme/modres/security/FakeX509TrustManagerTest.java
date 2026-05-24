package com.acme.modres.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class FakeX509TrustManagerTest {

    private FakeX509TrustManager trustManager;

    @BeforeEach
    void setUp() {
        trustManager = new FakeX509TrustManager();
    }

    @Test
    void testConstructor_shouldCreateInstance() {
        assertNotNull(trustManager);
    }

    @Test
    void testConstructor_shouldNotThrowException() {
        assertDoesNotThrow(() -> new FakeX509TrustManager());
    }

    @Test
    void testInstance_shouldBeCreatable() {
        FakeX509TrustManager manager = new FakeX509TrustManager();
        assertNotNull(manager);
    }
}
