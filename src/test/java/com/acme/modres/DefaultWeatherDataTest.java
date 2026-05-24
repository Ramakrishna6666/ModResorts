package com.acme.modres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class DefaultWeatherDataTest {

    @Test
    void testConstructor_withValidCity_shouldCreateInstance() {
        DefaultWeatherData data = new DefaultWeatherData("Paris");
        assertNotNull(data);
    }

    @Test
    void testConstructor_withNullCity_shouldThrowException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData(null);
        });
    }

    @Test
    void testConstructor_withInvalidCity_shouldThrowException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData("InvalidCity");
        });
    }

    @Test
    void testGetCity_shouldReturnCity() {
        DefaultWeatherData data = new DefaultWeatherData("Paris");
        assertEquals("Paris", data.getCity());
    }

    @Test
    void testConstructor_withParis_shouldCreateInstance() {
        DefaultWeatherData data = new DefaultWeatherData("Paris");
        assertEquals("Paris", data.getCity());
    }

    @Test
    void testConstructor_withLasVegas_shouldCreateInstance() {
        DefaultWeatherData data = new DefaultWeatherData("Las_Vegas");
        assertEquals("Las_Vegas", data.getCity());
    }

    @Test
    void testConstructor_withSanFrancisco_shouldCreateInstance() {
        DefaultWeatherData data = new DefaultWeatherData("San_Francisco");
        assertEquals("San_Francisco", data.getCity());
    }

    @Test
    void testConstructor_withMiami_shouldCreateInstance() {
        DefaultWeatherData data = new DefaultWeatherData("Miami");
        assertEquals("Miami", data.getCity());
    }

    @Test
    void testConstructor_withCork_shouldCreateInstance() {
        DefaultWeatherData data = new DefaultWeatherData("Cork");
        assertEquals("Cork", data.getCity());
    }

    @Test
    void testConstructor_withBarcelona_shouldCreateInstance() {
        DefaultWeatherData data = new DefaultWeatherData("Barcelona");
        assertEquals("Barcelona", data.getCity());
    }

    @Test
    void testGetDefaultWeatherData_shouldReturnString() throws Exception {
        DefaultWeatherData data = new DefaultWeatherData("Paris");
        // May throw IOException if resource not found
        assertDoesNotThrow(() -> data.getDefaultWeatherData());
    }

    @Test
    void testConstructor_withEmptyString_shouldThrowException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData("");
        });
    }
}
