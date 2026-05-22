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
        reservations.add(new Reservation("09/01/2024", "09/10/2024"));
        
        ReservationList list = new ReservationList(reservations);
        
        assertNotNull(list);
        assertEquals(2, list.getReservations().size());
    }

    @Test
    void testAdd_singleReservation() {
        Reservation reservation = new Reservation("08/10/2024", "08/20/2024");
        reservationList.add(reservation);
        
        assertEquals(1, reservationList.getReservations().size());
        assertEquals("08/10/2024", reservationList.getReservations().get(0).getFromDate());
    }

    @Test
    void testAdd_multipleReservations() {
        Reservation reservation1 = new Reservation("08/10/2024", "08/20/2024");
        Reservation reservation2 = new Reservation("09/01/2024", "09/10/2024");
        Reservation reservation3 = new Reservation("10/01/2024", "10/10/2024");
        
        reservationList.add(reservation1);
        reservationList.add(reservation2);
        reservationList.add(reservation3);
        
        assertEquals(3, reservationList.getReservations().size());
    }

    @Test
    void testGetReservations() {
        List<Reservation> reservations = reservationList.getReservations();
        
        assertNotNull(reservations);
        assertTrue(reservations.isEmpty());
    }

    @Test
    void testGetReservations_afterAdding() {
        Reservation reservation = new Reservation("08/10/2024", "08/20/2024");
        reservationList.add(reservation);
        
        List<Reservation> reservations = reservationList.getReservations();
        
        assertNotNull(reservations);
        assertEquals(1, reservations.size());
        assertEquals(reservation, reservations.get(0));
    }

    @Test
    void testAdd_nullReservation() {
        reservationList.add(null);
        
        assertEquals(1, reservationList.getReservations().size());
        assertNull(reservationList.getReservations().get(0));
    }

    @Test
    void testParameterizedConstructor_withEmptyList() {
        List<Reservation> emptyList = new ArrayList<>();
        ReservationList list = new ReservationList(emptyList);
        
        assertNotNull(list);
        assertTrue(list.getReservations().isEmpty());
    }

    @Test
    void testParameterizedConstructor_withNullList() {
        ReservationList list = new ReservationList(null);
        
        assertNotNull(list);
        assertNull(list.getReservations());
    }

    @Test
    void testGetReservations_returnsModifiableList() {
        List<Reservation> reservations = reservationList.getReservations();
        reservations.add(new Reservation("08/10/2024", "08/20/2024"));
        
        assertEquals(1, reservationList.getReservations().size());
    }

    @Test
    void testAdd_preservesOrder() {
        Reservation reservation1 = new Reservation("08/10/2024", "08/20/2024");
        Reservation reservation2 = new Reservation("09/01/2024", "09/10/2024");
        
        reservationList.add(reservation1);
        reservationList.add(reservation2);
        
        assertEquals(reservation1, reservationList.getReservations().get(0));
        assertEquals(reservation2, reservationList.getReservations().get(1));
    }

    @Test
    void testMultipleAdds() {
        for (int i = 0; i < 10; i++) {
            reservationList.add(new Reservation("08/" + (10 + i) + "/2024", "08/" + (20 + i) + "/2024"));
        }
        
        assertEquals(10, reservationList.getReservations().size());
    }
}
