package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservationListTest {

    private ReservationList reservationList;

    @BeforeEach
    void setUp() {
        reservationList = new ReservationList();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(reservationList);
        assertNotNull(reservationList.getReservations());
        assertTrue(reservationList.getReservations().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("08/10/2024", "08/20/2024"));
        
        ReservationList list = new ReservationList(reservations);
        
        assertNotNull(list);
        assertEquals(1, list.getReservations().size());
    }

    @Test
    void testAdd() {
        Reservation reservation = new Reservation("08/10/2024", "08/20/2024");
        reservationList.add(reservation);
        
        assertEquals(1, reservationList.getReservations().size());
    }

    @Test
    void testAddMultiple() {
        reservationList.add(new Reservation("08/10/2024", "08/20/2024"));
        reservationList.add(new Reservation("09/01/2024", "09/10/2024"));
        reservationList.add(new Reservation("10/01/2024", "10/10/2024"));
        
        assertEquals(3, reservationList.getReservations().size());
    }

    @Test
    void testGetReservations() {
        List<Reservation> reservations = reservationList.getReservations();
        assertNotNull(reservations);
        assertTrue(reservations instanceof ArrayList);
    }

    @Test
    void testAdd_maintainsOrder() {
        Reservation res1 = new Reservation("08/10/2024", "08/20/2024");
        Reservation res2 = new Reservation("09/01/2024", "09/10/2024");
        
        reservationList.add(res1);
        reservationList.add(res2);
        
        List<Reservation> reservations = reservationList.getReservations();
        assertEquals("08/10/2024", reservations.get(0).getFromDate());
        assertEquals("09/01/2024", reservations.get(1).getFromDate());
    }

    @Test
    void testAdd_withNull() {
        reservationList.add(null);
        assertEquals(1, reservationList.getReservations().size());
    }

    @Test
    void testParameterizedConstructor_withNull() {
        ReservationList list = new ReservationList(null);
        assertNotNull(list);
    }

    @Test
    void testParameterizedConstructor_withEmptyList() {
        List<Reservation> emptyList = new ArrayList<>();
        ReservationList list = new ReservationList(emptyList);
        
        assertNotNull(list);
        assertTrue(list.getReservations().isEmpty());
    }

    @Test
    void testGetReservations_returnsCorrectSize() {
        reservationList.add(new Reservation("08/10/2024", "08/20/2024"));
        reservationList.add(new Reservation("09/01/2024", "09/10/2024"));
        
        assertEquals(2, reservationList.getReservations().size());
    }
}
