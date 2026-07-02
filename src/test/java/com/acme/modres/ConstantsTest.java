package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void testCityConstants() {
        assertEquals("Barcelona", Constants.BARCELONA);
        assertEquals("Cork", Constants.CORK);
        assertEquals("Miami", Constants.MIAMI);
        assertEquals("San_Francisco", Constants.SAN_FRANCISCO);
        assertEquals("Paris", Constants.PARIS);
        assertEquals("Las_Vegas", Constants.LAS_VEGAS);
    }

    @Test
    void testSupportedCitiesArray() {
        assertNotNull(Constants.SUPPORTED_CITIES);
        assertEquals(6, Constants.SUPPORTED_CITIES.length);
        assertTrue(containsCity(Constants.SUPPORTED_CITIES, Constants.PARIS));
        assertTrue(containsCity(Constants.SUPPORTED_CITIES, Constants.LAS_VEGAS));
        assertTrue(containsCity(Constants.SUPPORTED_CITIES, Constants.SAN_FRANCISCO));
        assertTrue(containsCity(Constants.SUPPORTED_CITIES, Constants.MIAMI));
        assertTrue(containsCity(Constants.SUPPORTED_CITIES, Constants.CORK));
        assertTrue(containsCity(Constants.SUPPORTED_CITIES, Constants.BARCELONA));
    }

    @Test
    void testWeatherFileConstants() {
        assertEquals("barcelona.json", Constants.BACELONA_WEATHER_FILE);
        assertEquals("cork.json", Constants.CORK_WEATHER_FILE);
        assertEquals("nv.json", Constants.LAS_VEGAS_WEATHER_FILE);
        assertEquals("miami.json", Constants.MIAMI_WEATHER_FILE);
        assertEquals("paris.json", Constants.PARIS_WEATHER_FILE);
        assertEquals("sanfran.json", Constants.SAN_FRANCESCO_WEATHER_FILE);
    }

    @Test
    void testWundergroundAPIConstants() {
        assertEquals("http://api.wunderground.com/api/", Constants.WUNDERGROUND_API_PREFIX);
        assertEquals("/forecast/geolookup/conditions/q/", Constants.WUNDERGROUND_API_PART);
    }

    @Test
    void testDateFormatConstant() {
        assertEquals("MM/dd/yyyy", Constants.DATA_FORMAT);
    }

    @Test
    void testConstantsNotNull() {
        assertNotNull(Constants.BARCELONA);
        assertNotNull(Constants.CORK);
        assertNotNull(Constants.MIAMI);
        assertNotNull(Constants.SAN_FRANCISCO);
        assertNotNull(Constants.PARIS);
        assertNotNull(Constants.LAS_VEGAS);
    }

    @Test
    void testWeatherFilesNotNull() {
        assertNotNull(Constants.BACELONA_WEATHER_FILE);
        assertNotNull(Constants.CORK_WEATHER_FILE);
        assertNotNull(Constants.LAS_VEGAS_WEATHER_FILE);
        assertNotNull(Constants.MIAMI_WEATHER_FILE);
        assertNotNull(Constants.PARIS_WEATHER_FILE);
        assertNotNull(Constants.SAN_FRANCESCO_WEATHER_FILE);
    }

    @Test
    void testAPIConstantsNotNull() {
        assertNotNull(Constants.WUNDERGROUND_API_PREFIX);
        assertNotNull(Constants.WUNDERGROUND_API_PART);
        assertNotNull(Constants.DATA_FORMAT);
    }

    private boolean containsCity(String[] cities, String city) {
        for (String c : cities) {
            if (c.equals(city)) {
                return true;
            }
        }
        return false;
    }
}
