package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(reservation);
    }

    @Test
    void testParameterizedConstructor() {
        Reservation res = new Reservation("08/10/2024", "08/20/2024");
        
        assertNotNull(res);
        assertEquals("08/10/2024", res.getFromDate());
        assertEquals("08/20/2024", res.getToDate());
    }

    @Test
    void testSetFromDate() {
        reservation.setFromDate("08/10/2024");
        assertEquals("08/10/2024", reservation.getFromDate());
    }

    @Test
    void testSetToDate() {
        reservation.setToDate("08/20/2024");
        assertEquals("08/20/2024", reservation.getToDate());
    }

    @Test
    void testGetFromDate() {
        reservation.setFromDate("08/15/2024");
        assertEquals("08/15/2024", reservation.getFromDate());
    }

    @Test
    void testGetToDate() {
        reservation.setToDate("08/25/2024");
        assertEquals("08/25/2024", reservation.getToDate());
    }

    @Test
    void testSetFromDate_withNull() {
        reservation.setFromDate(null);
        assertNull(reservation.getFromDate());
    }

    @Test
    void testSetToDate_withNull() {
        reservation.setToDate(null);
        assertNull(reservation.getToDate());
    }

    @Test
    void testSetFromDate_withEmptyString() {
        reservation.setFromDate("");
        assertEquals("", reservation.getFromDate());
    }

    @Test
    void testSetToDate_withEmptyString() {
        reservation.setToDate("");
        assertEquals("", reservation.getToDate());
    }

    @Test
    void testParameterizedConstructor_withNullValues() {
        Reservation res = new Reservation(null, null);
        assertNull(res.getFromDate());
        assertNull(res.getToDate());
    }

    @Test
    void testSetAndGetDates() {
        reservation.setFromDate("01/01/2024");
        reservation.setToDate("12/31/2024");
        
        assertEquals("01/01/2024", reservation.getFromDate());
        assertEquals("12/31/2024", reservation.getToDate());
    }
}
