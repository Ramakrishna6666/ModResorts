package com.acme.modres.mbean.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationListTest {

    private ReservationList reservationList;

    @BeforeEach
    void setUp() {
        reservationList = new ReservationList();
    }

    @Test
    void testDefaultConstructor_shouldCreateEmptyList() {
        assertNotNull(reservationList);
        assertNotNull(reservationList.getReservations());
        assertTrue(reservationList.getReservations().isEmpty());
    }

    @Test
    void testParameterizedConstructor_shouldSetReservations() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("01/01/2024", "01/10/2024"));
        ReservationList list = new ReservationList(reservations);
        assertEquals(1, list.getReservations().size());
    }

    @Test
    void testAdd_shouldAddReservation() {
        Reservation reservation = new Reservation("01/01/2024", "01/10/2024");
        reservationList.add(reservation);
        assertEquals(1, reservationList.getReservations().size());
    }

    @Test
    void testAdd_multipleReservations_shouldAddAll() {
        reservationList.add(new Reservation("01/01/2024", "01/10/2024"));
        reservationList.add(new Reservation("02/01/2024", "02/10/2024"));
        assertEquals(2, reservationList.getReservations().size());
    }

    @Test
    void testGetReservations_shouldReturnList() {
        List<Reservation> reservations = reservationList.getReservations();
        assertNotNull(reservations);
    }

    @Test
    void testAdd_withNullReservation_shouldAddNull() {
        reservationList.add(null);
        assertEquals(1, reservationList.getReservations().size());
    }

    @Test
    void testParameterizedConstructor_withNull_shouldSetNull() {
        ReservationList list = new ReservationList(null);
        assertNull(list.getReservations());
    }

    @Test
    void testAdd_afterMultipleAdds_shouldMaintainOrder() {
        Reservation res1 = new Reservation("01/01/2024", "01/10/2024");
        Reservation res2 = new Reservation("02/01/2024", "02/10/2024");
        reservationList.add(res1);
        reservationList.add(res2);
        assertEquals(res1, reservationList.getReservations().get(0));
        assertEquals(res2, reservationList.getReservations().get(1));
    }
}
