package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void testDefaultConstructor() {
        Reservation reservation = new Reservation();
        
        assertNotNull(reservation);
        assertNull(reservation.getFromDate());
        assertNull(reservation.getToDate());
    }

    @Test
    void testParameterizedConstructor() {
        Reservation reservation = new Reservation("08/10/2024", "08/20/2024");
        
        assertNotNull(reservation);
        assertEquals("08/10/2024", reservation.getFromDate());
        assertEquals("08/20/2024", reservation.getToDate());
    }

    @Test
    void testSetFromDate() {
        Reservation reservation = new Reservation();
        reservation.setFromDate("08/15/2024");
        
        assertEquals("08/15/2024", reservation.getFromDate());
    }

    @Test
    void testSetToDate() {
        Reservation reservation = new Reservation();
        reservation.setToDate("08/25/2024");
        
        assertEquals("08/25/2024", reservation.getToDate());
    }

    @Test
    void testGetFromDate() {
        Reservation reservation = new Reservation("08/10/2024", "08/20/2024");
        
        assertEquals("08/10/2024", reservation.getFromDate());
    }

    @Test
    void testGetToDate() {
        Reservation reservation = new Reservation("08/10/2024", "08/20/2024");
        
        assertEquals("08/20/2024", reservation.getToDate());
    }

    @Test
    void testSetFromDate_withNull() {
        Reservation reservation = new Reservation();
        reservation.setFromDate(null);
        
        assertNull(reservation.getFromDate());
    }

    @Test
    void testSetToDate_withNull() {
        Reservation reservation = new Reservation();
        reservation.setToDate(null);
        
        assertNull(reservation.getToDate());
    }

    @Test
    void testConstructor_withNullDates() {
        Reservation reservation = new Reservation(null, null);
        
        assertNotNull(reservation);
        assertNull(reservation.getFromDate());
        assertNull(reservation.getToDate());
    }

    @Test
    void testSetFromDate_withEmptyString() {
        Reservation reservation = new Reservation();
        reservation.setFromDate("");
        
        assertEquals("", reservation.getFromDate());
    }

    @Test
    void testSetToDate_withEmptyString() {
        Reservation reservation = new Reservation();
        reservation.setToDate("");
        
        assertEquals("", reservation.getToDate());
    }

    @Test
    void testMultipleSettersOnSameObject() {
        Reservation reservation = new Reservation();
        reservation.setFromDate("08/10/2024");
        reservation.setToDate("08/20/2024");
        
        assertEquals("08/10/2024", reservation.getFromDate());
        assertEquals("08/20/2024", reservation.getToDate());
        
        reservation.setFromDate("09/01/2024");
        reservation.setToDate("09/10/2024");
        
        assertEquals("09/01/2024", reservation.getFromDate());
        assertEquals("09/10/2024", reservation.getToDate());
    }
}
