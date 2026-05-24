package com.acme.modres.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class SSLUtilsTest {

    private SSLUtils sslUtils;

    @BeforeEach
    void setUp() {
        sslUtils = new SSLUtils();
    }

    @Test
    void testConstructor_shouldCreateInstance() {
        assertNotNull(sslUtils);
    }

    @Test
    void testConstructor_shouldNotThrowException() {
        assertDoesNotThrow(() -> new SSLUtils());
    }

    @Test
    void testInstance_shouldBeCreatable() {
        SSLUtils utils = new SSLUtils();
        assertNotNull(utils);
    }
}
