package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservationCheckerDataTest {

    private ReservationList reservationList;
    private ReservationCheckerData checkerData;

    @BeforeEach
    void setUp() {
        reservationList = new ReservationList();
        reservationList.add(new Reservation("08/10/2024", "08/20/2024"));
        checkerData = new ReservationCheckerData(reservationList);
    }

    @Test
    void testConstructor() {
        assertNotNull(checkerData);
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testGetReservationList() {
        ReservationList list = checkerData.getReservationList();
        assertNotNull(list);
        assertEquals(reservationList, list);
    }

    @Test
    void testSetSelectedDate_withValidDate() {
        boolean result = checkerData.setSelectedDate("08/15/2024");
        assertTrue(result);
        assertNotNull(checkerData.getSelectedDate());
    }

    @Test
    void testSetSelectedDate_withInvalidDate() {
        boolean result = checkerData.setSelectedDate("invalid-date");
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withNullDate() {
        boolean result = checkerData.setSelectedDate(null);
        assertFalse(result);
    }

    @Test
    void testGetSelectedDate() {
        checkerData.setSelectedDate("08/15/2024");
        Date date = checkerData.getSelectedDate();
        assertNotNull(date);
    }

    @Test
    void testIsAvailible_defaultValue() {
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_true() {
        checkerData.setAvailablility(true);
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_false() {
        checkerData.setAvailablility(false);
        assertFalse(checkerData.isAvailible());
    }

    @Test
    void testSetSelectedDate_withEmptyString() {
        boolean result = checkerData.setSelectedDate("");
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withDifferentFormats() {
        boolean result1 = checkerData.setSelectedDate("08/15/2024");
        assertTrue(result1);
        
        boolean result2 = checkerData.setSelectedDate("2024-08-15");
        assertFalse(result2);
    }

    @Test
    void testConstructor_withNullReservationList() {
        ReservationCheckerData data = new ReservationCheckerData(null);
        assertNotNull(data);
        assertNull(data.getReservationList());
    }

    @Test
    void testSetAvailablility_toggles() {
        checkerData.setAvailablility(false);
        assertFalse(checkerData.isAvailible());
        
        checkerData.setAvailablility(true);
        assertTrue(checkerData.isAvailible());
    }
}
