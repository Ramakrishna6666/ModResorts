package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class DefaultWeatherDataTest {

    @Test
    void testConstructor_withValidCity_Paris() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.PARIS);
        assertEquals(Constants.PARIS, data.getCity());
    }

    @Test
    void testConstructor_withValidCity_LasVegas() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.LAS_VEGAS);
        assertEquals(Constants.LAS_VEGAS, data.getCity());
    }

    @Test
    void testConstructor_withValidCity_SanFrancisco() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.SAN_FRANCISCO);
        assertEquals(Constants.SAN_FRANCISCO, data.getCity());
    }

    @Test
    void testConstructor_withValidCity_Miami() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.MIAMI);
        assertEquals(Constants.MIAMI, data.getCity());
    }

    @Test
    void testConstructor_withValidCity_Cork() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.CORK);
        assertEquals(Constants.CORK, data.getCity());
    }

    @Test
    void testConstructor_withValidCity_Barcelona() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.BARCELONA);
        assertEquals(Constants.BARCELONA, data.getCity());
    }

    @Test
    void testConstructor_withNullCity_throwsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData(null);
        });
    }

    @Test
    void testConstructor_withInvalidCity_throwsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData("InvalidCity");
        });
    }

    @Test
    void testConstructor_withEmptyCity_throwsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData("");
        });
    }

    @Test
    void testGetDefaultWeatherData_Paris() throws IOException {
        DefaultWeatherData data = new DefaultWeatherData(Constants.PARIS);
        String weatherData = data.getDefaultWeatherData();
        assertNotNull(weatherData);
    }

    @Test
    void testGetDefaultWeatherData_LasVegas() throws IOException {
        DefaultWeatherData data = new DefaultWeatherData(Constants.LAS_VEGAS);
        String weatherData = data.getDefaultWeatherData();
        assertNotNull(weatherData);
    }

    @Test
    void testGetDefaultWeatherData_SanFrancisco() throws IOException {
        DefaultWeatherData data = new DefaultWeatherData(Constants.SAN_FRANCISCO);
        String weatherData = data.getDefaultWeatherData();
        assertNotNull(weatherData);
    }

    @Test
    void testGetDefaultWeatherData_Miami() throws IOException {
        DefaultWeatherData data = new DefaultWeatherData(Constants.MIAMI);
        String weatherData = data.getDefaultWeatherData();
        assertNotNull(weatherData);
    }

    @Test
    void testGetDefaultWeatherData_Cork() throws IOException {
        DefaultWeatherData data = new DefaultWeatherData(Constants.CORK);
        String weatherData = data.getDefaultWeatherData();
        assertNotNull(weatherData);
    }

    @Test
    void testGetDefaultWeatherData_Barcelona() throws IOException {
        DefaultWeatherData data = new DefaultWeatherData(Constants.BARCELONA);
        String weatherData = data.getDefaultWeatherData();
        assertNotNull(weatherData);
    }

    @Test
    void testGetCity() {
        DefaultWeatherData data = new DefaultWeatherData(Constants.PARIS);
        assertEquals(Constants.PARIS, data.getCity());
    }
}
