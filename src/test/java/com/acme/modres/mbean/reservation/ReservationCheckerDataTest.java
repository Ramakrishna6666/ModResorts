package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservationCheckerDataTest {

    private ReservationList reservationList;
    private ReservationCheckerData checkerData;

    @BeforeEach
    void setUp() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("08/10/2024", "08/20/2024"));
        reservationList = new ReservationList(reservations);
        checkerData = new ReservationCheckerData(reservationList);
    }

    @Test
    void testConstructor() {
        assertNotNull(checkerData);
        assertEquals(reservationList, checkerData.getReservationList());
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testGetReservationList() {
        ReservationList result = checkerData.getReservationList();
        
        assertNotNull(result);
        assertEquals(reservationList, result);
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
    void testSetSelectedDate_withEmptyDate() {
        boolean result = checkerData.setSelectedDate("");
        
        assertFalse(result);
    }

    @Test
    void testGetSelectedDate() {
        checkerData.setSelectedDate("08/15/2024");
        Date selectedDate = checkerData.getSelectedDate();
        
        assertNotNull(selectedDate);
    }

    @Test
    void testIsAvailible_defaultValue() {
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_toFalse() {
        checkerData.setAvailablility(false);
        
        assertFalse(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_toTrue() {
        checkerData.setAvailablility(false);
        checkerData.setAvailablility(true);
        
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetSelectedDate_withDifferentFormats() {
        boolean result1 = checkerData.setSelectedDate("12/31/2024");
        assertTrue(result1);
        
        boolean result2 = checkerData.setSelectedDate("01/01/2025");
        assertTrue(result2);
    }

    @Test
    void testConstructor_withNullReservationList() {
        ReservationCheckerData data = new ReservationCheckerData(null);
        
        assertNotNull(data);
        assertNull(data.getReservationList());
    }

    @Test
    void testSetSelectedDate_withLeapYearDate() {
        boolean result = checkerData.setSelectedDate("02/29/2024");
        
        assertTrue(result);
    }

    @Test
    void testSetSelectedDate_withInvalidLeapYearDate() {
        boolean result = checkerData.setSelectedDate("02/29/2023");
        
        assertFalse(result);
    }

    @Test
    void testMultipleSetSelectedDate() {
        checkerData.setSelectedDate("08/15/2024");
        Date firstDate = checkerData.getSelectedDate();
        
        checkerData.setSelectedDate("09/15/2024");
        Date secondDate = checkerData.getSelectedDate();
        
        assertNotEquals(firstDate, secondDate);
    }

    @Test
    void testAvailabilityToggle() {
        assertTrue(checkerData.isAvailible());
        
        checkerData.setAvailablility(false);
        assertFalse(checkerData.isAvailible());
        
        checkerData.setAvailablility(true);
        assertTrue(checkerData.isAvailible());
    }
}
