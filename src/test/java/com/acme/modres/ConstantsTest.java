package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void testBarcelonaConstant() {
        assertEquals("Barcelona", Constants.BARCELONA);
    }

    @Test
    void testCorkConstant() {
        assertEquals("Cork", Constants.CORK);
    }

    @Test
    void testMiamiConstant() {
        assertEquals("Miami", Constants.MIAMI);
    }

    @Test
    void testSanFranciscoConstant() {
        assertEquals("San_Francisco", Constants.SAN_FRANCISCO);
    }

    @Test
    void testParisConstant() {
        assertEquals("Paris", Constants.PARIS);
    }

    @Test
    void testLasVegasConstant() {
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
    void testBarcelonaWeatherFile() {
        assertEquals("barcelona.json", Constants.BACELONA_WEATHER_FILE);
    }

    @Test
    void testCorkWeatherFile() {
        assertEquals("cork.json", Constants.CORK_WEATHER_FILE);
    }

    @Test
    void testLasVegasWeatherFile() {
        assertEquals("nv.json", Constants.LAS_VEGAS_WEATHER_FILE);
    }

    @Test
    void testMiamiWeatherFile() {
        assertEquals("miami.json", Constants.MIAMI_WEATHER_FILE);
    }

    @Test
    void testParisWeatherFile() {
        assertEquals("paris.json", Constants.PARIS_WEATHER_FILE);
    }

    @Test
    void testSanFranciscoWeatherFile() {
        assertEquals("sanfran.json", Constants.SAN_FRANCESCO_WEATHER_FILE);
    }

    @Test
    void testWundergroundApiPrefix() {
        assertEquals("http://api.wunderground.com/api/", Constants.WUNDERGROUND_API_PREFIX);
    }

    @Test
    void testWundergroundApiPart() {
        assertEquals("/forecast/geolookup/conditions/q/", Constants.WUNDERGROUND_API_PART);
    }

    @Test
    void testDataFormat() {
        assertEquals("MM/dd/yyyy", Constants.DATA_FORMAT);
    }

    @Test
    void testConstantsConstructor() {
        assertDoesNotThrow(() -> new Constants());
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
