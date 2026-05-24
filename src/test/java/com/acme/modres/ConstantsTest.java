package com.acme.modres;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantsTest {

    @Test
    void testBarcelonaConstant_shouldHaveCorrectValue() {
        assertEquals("Barcelona", Constants.BARCELONA);
    }

    @Test
    void testCorkConstant_shouldHaveCorrectValue() {
        assertEquals("Cork", Constants.CORK);
    }

    @Test
    void testMiamiConstant_shouldHaveCorrectValue() {
        assertEquals("Miami", Constants.MIAMI);
    }

    @Test
    void testSanFranciscoConstant_shouldHaveCorrectValue() {
        assertEquals("San_Francisco", Constants.SAN_FRANCISCO);
    }

    @Test
    void testParisConstant_shouldHaveCorrectValue() {
        assertEquals("Paris", Constants.PARIS);
    }

    @Test
    void testLasVegasConstant_shouldHaveCorrectValue() {
        assertEquals("Las_Vegas", Constants.LAS_VEGAS);
    }

    @Test
    void testSupportedCities_shouldContainAllCities() {
        assertEquals(6, Constants.SUPPORTED_CITIES.length);
    }

    @Test
    void testSupportedCities_shouldContainParis() {
        boolean containsParis = false;
        for (String city : Constants.SUPPORTED_CITIES) {
            if ("Paris".equals(city)) {
                containsParis = true;
                break;
            }
        }
        assertTrue(containsParis);
    }

    @Test
    void testBarcelonaWeatherFile_shouldHaveCorrectValue() {
        assertEquals("barcelona.json", Constants.BACELONA_WEATHER_FILE);
    }

    @Test
    void testCorkWeatherFile_shouldHaveCorrectValue() {
        assertEquals("cork.json", Constants.CORK_WEATHER_FILE);
    }

    @Test
    void testLasVegasWeatherFile_shouldHaveCorrectValue() {
        assertEquals("nv.json", Constants.LAS_VEGAS_WEATHER_FILE);
    }

    @Test
    void testMiamiWeatherFile_shouldHaveCorrectValue() {
        assertEquals("miami.json", Constants.MIAMI_WEATHER_FILE);
    }

    @Test
    void testParisWeatherFile_shouldHaveCorrectValue() {
        assertEquals("paris.json", Constants.PARIS_WEATHER_FILE);
    }

    @Test
    void testSanFranciscoWeatherFile_shouldHaveCorrectValue() {
        assertEquals("sanfran.json", Constants.SAN_FRANCESCO_WEATHER_FILE);
    }

    @Test
    void testWundergroundApiPrefix_shouldHaveCorrectValue() {
        assertEquals("http://api.wunderground.com/api/", Constants.WUNDERGROUND_API_PREFIX);
    }

    @Test
    void testWundergroundApiPart_shouldHaveCorrectValue() {
        assertEquals("/forecast/geolookup/conditions/q/", Constants.WUNDERGROUND_API_PART);
    }

    @Test
    void testDataFormat_shouldHaveCorrectValue() {
        assertEquals("MM/dd/yyyy", Constants.DATA_FORMAT);
    }

    @Test
    void testConstructor_shouldCreateInstance() {
        Constants constants = new Constants();
        assertNotNull(constants);
    }
}
