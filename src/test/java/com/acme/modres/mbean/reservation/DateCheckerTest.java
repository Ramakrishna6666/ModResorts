package com.acme.modres.mbean.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class DateCheckerTest {

    private DateChecker dateChecker;
    private ReservationCheckerData checkerData;
    private ReservationList reservationList;

    @BeforeEach
    void setUp() {
        reservationList = new ReservationList();
        reservationList.add(new Reservation("01/01/2024", "01/10/2024"));
        reservationList.add(new Reservation("02/01/2024", "02/10/2024"));
        checkerData = new ReservationCheckerData(reservationList);
        dateChecker = new DateChecker(checkerData);
    }

    @Test
    void testConstructor_shouldCreateInstance() {
        assertNotNull(dateChecker);
    }

    @Test
    void testConstructor_shouldSetData() {
        DateChecker checker = new DateChecker(checkerData);
        assertNotNull(checker);
    }

    @Test
    void testRun_withValidDate_shouldExecute() {
        checkerData.setSelectedDate("01/05/2024");
        assertDoesNotThrow(() -> dateChecker.run());
    }

    @Test
    void testRun_withDateOutsideRange_shouldSetAvailableTrue() {
        checkerData.setSelectedDate("03/15/2024");
        dateChecker.run();
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testRun_withDateInsideRange_shouldSetAvailableFalse() {
        checkerData.setSelectedDate("01/05/2024");
        dateChecker.run();
        assertFalse(checkerData.isAvailible());
    }

    @Test
    void testRun_withInvalidDateFormat_shouldHandleGracefully() {
        checkerData.setSelectedDate("invalid-date");
        assertDoesNotThrow(() -> dateChecker.run());
    }

    @Test
    void testRun_withEmptyReservationList_shouldSetAvailableTrue() {
        ReservationList emptyList = new ReservationList();
        ReservationCheckerData emptyData = new ReservationCheckerData(emptyList);
        emptyData.setSelectedDate("01/15/2024");
        DateChecker checker = new DateChecker(emptyData);
        checker.run();
        assertTrue(emptyData.isAvailible());
    }

    @Test
    void testRun_shouldImplementRunnable() {
        assertTrue(dateChecker instanceof Runnable);
    }
}
