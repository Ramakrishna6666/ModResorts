package com.acme.modres.mbean.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
    }

    @Test
    void testDefaultConstructor_shouldCreateInstance() {
        assertNotNull(reservation);
    }

    @Test
    void testParameterizedConstructor_shouldSetDates() {
        Reservation res = new Reservation("01/01/2024", "01/10/2024");
        assertEquals("01/01/2024", res.getFromDate());
        assertEquals("01/10/2024", res.getToDate());
    }

    @Test
    void testSetFromDate_shouldSetDate() {
        reservation.setFromDate("02/01/2024");
        assertEquals("02/01/2024", reservation.getFromDate());
    }

    @Test
    void testSetToDate_shouldSetDate() {
        reservation.setToDate("02/15/2024");
        assertEquals("02/15/2024", reservation.getToDate());
    }

    @Test
    void testGetFromDate_withNullValue_shouldReturnNull() {
        assertNull(reservation.getFromDate());
    }

    @Test
    void testGetToDate_withNullValue_shouldReturnNull() {
        assertNull(reservation.getToDate());
    }

    @Test
    void testSetFromDate_withNull_shouldSetNull() {
        reservation.setFromDate(null);
        assertNull(reservation.getFromDate());
    }

    @Test
    void testSetToDate_withNull_shouldSetNull() {
        reservation.setToDate(null);
        assertNull(reservation.getToDate());
    }

    @Test
    void testSetFromDate_withEmptyString_shouldSetEmpty() {
        reservation.setFromDate("");
        assertEquals("", reservation.getFromDate());
    }

    @Test
    void testSetToDate_withEmptyString_shouldSetEmpty() {
        reservation.setToDate("");
        assertEquals("", reservation.getToDate());
    }
}
