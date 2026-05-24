package com.acme.modres.mbean.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ReservationCheckerDataTest {

    private ReservationCheckerData checkerData;
    private ReservationList reservationList;

    @BeforeEach
    void setUp() {
        reservationList = new ReservationList();
        checkerData = new ReservationCheckerData(reservationList);
    }

    @Test
    void testConstructor_shouldSetReservationList() {
        assertNotNull(checkerData.getReservationList());
        assertEquals(reservationList, checkerData.getReservationList());
    }

    @Test
    void testConstructor_shouldSetAvailableToTrue() {
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testGetReservationList_shouldReturnList() {
        ReservationList list = checkerData.getReservationList();
        assertNotNull(list);
    }

    @Test
    void testSetSelectedDate_withValidDate_shouldReturnTrue() {
        boolean result = checkerData.setSelectedDate("01/15/2024");
        assertTrue(result);
    }

    @Test
    void testSetSelectedDate_withInvalidDate_shouldReturnFalse() {
        boolean result = checkerData.setSelectedDate("invalid-date");
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withNull_shouldReturnFalse() {
        boolean result = checkerData.setSelectedDate(null);
        assertFalse(result);
    }

    @Test
    void testGetSelectedLocalDate_afterSettingDate_shouldReturnLocalDate() {
        checkerData.setSelectedDate("01/15/2024");
        LocalDate date = checkerData.getSelectedLocalDate();
        assertNotNull(date);
    }

    @Test
    void testGetSelectedDate_withNullDate_shouldReturnNull() {
        Date date = checkerData.getSelectedDate();
        assertNull(date);
    }

    @Test
    void testGetSelectedDate_afterSettingDate_shouldReturnDate() {
        checkerData.setSelectedDate("01/15/2024");
        Date date = checkerData.getSelectedDate();
        assertNotNull(date);
    }

    @Test
    void testIsAvailible_defaultValue_shouldReturnTrue() {
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_withFalse_shouldSetFalse() {
        checkerData.setAvailablility(false);
        assertFalse(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_withTrue_shouldSetTrue() {
        checkerData.setAvailablility(true);
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetSelectedDate_withEmptyString_shouldReturnFalse() {
        boolean result = checkerData.setSelectedDate("");
        assertFalse(result);
    }
}
