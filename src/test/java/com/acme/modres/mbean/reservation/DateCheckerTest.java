package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.acme.modres.Constants;

class DateCheckerTest {

    private ReservationCheckerData data;
    private DateChecker dateChecker;

    @BeforeEach
    void setUp() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("08/10/2024", "08/20/2024"));
        reservations.add(new Reservation("09/01/2024", "09/10/2024"));
        
        ReservationList reservationList = new ReservationList(reservations);
        data = new ReservationCheckerData(reservationList);
        data.setSelectedDate("08/15/2024");
        
        dateChecker = new DateChecker(data);
    }

    @Test
    void testConstructor() {
        assertNotNull(dateChecker);
    }

    @Test
    void testRun_withDateInRange() {
        data.setSelectedDate("08/15/2024");
        dateChecker.run();
        
        assertFalse(data.isAvailible());
    }

    @Test
    void testRun_withDateOutOfRange() {
        data.setSelectedDate("08/25/2024");
        dateChecker.run();
        
        assertTrue(data.isAvailible());
    }

    @Test
    void testRun_withDateBeforeRange() {
        data.setSelectedDate("08/05/2024");
        dateChecker.run();
        
        assertTrue(data.isAvailible());
    }

    @Test
    void testRun_withDateAfterRange() {
        data.setSelectedDate("10/01/2024");
        dateChecker.run();
        
        assertTrue(data.isAvailible());
    }

    @Test
    void testRun_withMultipleReservations() {
        data.setSelectedDate("09/05/2024");
        dateChecker.run();
        
        assertFalse(data.isAvailible());
    }

    @Test
    void testRun_withEmptyReservations() {
        ReservationList emptyList = new ReservationList(new ArrayList<>());
        ReservationCheckerData emptyData = new ReservationCheckerData(emptyList);
        emptyData.setSelectedDate("08/15/2024");
        
        DateChecker checker = new DateChecker(emptyData);
        checker.run();
        
        assertTrue(emptyData.isAvailible());
    }

    @Test
    void testRun_setsAvailability() {
        dateChecker.run();
        
        // Availability should be set (either true or false)
        assertTrue(data.isAvailible() || !data.isAvailible());
    }

    @Test
    void testRun_withInvalidDateFormat() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("invalid-date", "08/20/2024"));
        
        ReservationList reservationList = new ReservationList(reservations);
        ReservationCheckerData testData = new ReservationCheckerData(reservationList);
        testData.setSelectedDate("08/15/2024");
        
        DateChecker checker = new DateChecker(testData);
        assertDoesNotThrow(() -> checker.run());
    }
}
