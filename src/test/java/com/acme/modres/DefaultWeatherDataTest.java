package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class DefaultWeatherDataTest {

    @Test
    void testConstructor_withValidCity() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.PARIS);
        assertNotNull(data);
        assertEquals(Constants.PARIS, data.getCity());
    }

    @Test
    void testConstructor_withNullCity() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData(null);
        });
    }

    @Test
    void testConstructor_withInvalidCity() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData("InvalidCity");
        });
    }

    @Test
    void testGetCity_Paris() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.PARIS);
        assertEquals(Constants.PARIS, data.getCity());
    }

    @Test
    void testGetCity_LasVegas() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.LAS_VEGAS);
        assertEquals(Constants.LAS_VEGAS, data.getCity());
    }

    @Test
    void testGetCity_SanFrancisco() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.SAN_FRANCISCO);
        assertEquals(Constants.SAN_FRANCISCO, data.getCity());
    }

    @Test
    void testGetCity_Miami() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.MIAMI);
        assertEquals(Constants.MIAMI, data.getCity());
    }

    @Test
    void testGetCity_Cork() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.CORK);
        assertEquals(Constants.CORK, data.getCity());
    }

    @Test
    void testGetCity_Barcelona() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.BARCELONA);
        assertEquals(Constants.BARCELONA, data.getCity());
    }

    @Test
    void testGetDefaultWeatherData_Paris() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.PARIS);
        assertDoesNotThrow(() -> {
            String weatherData = data.getDefaultWeatherData();
            assertNotNull(weatherData);
        });
    }

    @Test
    void testGetDefaultWeatherData_LasVegas() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.LAS_VEGAS);
        assertDoesNotThrow(() -> {
            String weatherData = data.getDefaultWeatherData();
            assertNotNull(weatherData);
        });
    }

    @Test
    void testConstructor_withEmptyCity() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData("");
        });
    }

    @Test
    void testAllSupportedCities() {
        for (String city : Constants.SUPPORTED_CITIES) {
            assertDoesNotThrow(() -> {
                DefaultWeatherData data = new DefaultWeatherData(city);
                assertNotNull(data.getCity());
            });
        }
    }
}
