package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateCheckerTest {

    private ReservationCheckerData data;
    private ReservationList reservationList;

    @BeforeEach
    void setUp() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("08/10/2024", "08/20/2024"));
        reservations.add(new Reservation("09/01/2024", "09/10/2024"));
        reservationList = new ReservationList(reservations);
        data = new ReservationCheckerData(reservationList);
    }

    @Test
    void testConstructor() {
        DateChecker checker = new DateChecker(data);
        
        assertNotNull(checker);
    }

    @Test
    void testRun_withDateInRange_setsAvailabilityFalse() {
        data.setSelectedDate("08/15/2024");
        DateChecker checker = new DateChecker(data);
        
        checker.run();
        
        assertFalse(data.isAvailible());
    }

    @Test
    void testRun_withDateOutsideRange_setsAvailabilityTrue() {
        data.setSelectedDate("08/05/2024");
        DateChecker checker = new DateChecker(data);
        
        checker.run();
        
        assertTrue(data.isAvailible());
    }

    @Test
    void testRun_withDateBeforeAllReservations() {
        data.setSelectedDate("07/01/2024");
        DateChecker checker = new DateChecker(data);
        
        checker.run();
        
        assertTrue(data.isAvailible());
    }

    @Test
    void testRun_withDateAfterAllReservations() {
        data.setSelectedDate("10/01/2024");
        DateChecker checker = new DateChecker(data);
        
        checker.run();
        
        assertTrue(data.isAvailible());
    }

    @Test
    void testRun_withDateBetweenReservations() {
        data.setSelectedDate("08/25/2024");
        DateChecker checker = new DateChecker(data);
        
        checker.run();
        
        assertTrue(data.isAvailible());
    }

    @Test
    void testRun_withDateInSecondReservation() {
        data.setSelectedDate("09/05/2024");
        DateChecker checker = new DateChecker(data);
        
        checker.run();
        
        assertFalse(data.isAvailible());
    }

    @Test
    void testRun_withEmptyReservationList() {
        ReservationList emptyList = new ReservationList(new ArrayList<>());
        ReservationCheckerData emptyData = new ReservationCheckerData(emptyList);
        emptyData.setSelectedDate("08/15/2024");
        DateChecker checker = new DateChecker(emptyData);
        
        checker.run();
        
        assertTrue(emptyData.isAvailible());
    }

    @Test
    void testRun_asRunnable() {
        data.setSelectedDate("08/15/2024");
        Runnable checker = new DateChecker(data);
        
        assertDoesNotThrow(() -> checker.run());
    }

    @Test
    void testRun_withInvalidDateFormat() {
        data.setSelectedDate("invalid-date");
        DateChecker checker = new DateChecker(data);
        
        assertDoesNotThrow(() -> checker.run());
    }
}
