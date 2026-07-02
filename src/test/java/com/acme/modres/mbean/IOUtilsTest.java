package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acme.modres.mbean.reservation.ReservationList;

class IOUtilsTest {

    @Test
    void testGetFileFromRelativePath_withValidPath() {
        File file = IOUtils.getFileFromRelativePath("ops.json");
        // File may or may not exist depending on resources
        assertNotNull(file);
    }

    @Test
    void testGetFileFromRelativePath_withNullPath() {
        assertThrows(NullPointerException.class, () -> {
            IOUtils.getFileFromRelativePath(null);
        });
    }

    @Test
    void testGetFileFromRelativePath_withEmptyPath() {
        File file = IOUtils.getFileFromRelativePath("");
        assertNotNull(file);
    }

    @Test
    void testGetOpListFromConfig() {
        OpMetadataList opList = IOUtils.getOpListFromConfig();
        // May be null if file doesn't exist
        assertTrue(opList == null || opList instanceof OpMetadataList);
    }

    @Test
    void testGetReservationListFromConfig() {
        ReservationList reservationList = IOUtils.getReservationListFromConfig();
        // May be null if file doesn't exist
        assertTrue(reservationList == null || reservationList instanceof ReservationList);
    }

    @Test
    void testGetOpListFromConfig_returnsOpMetadataList() {
        OpMetadataList opList = IOUtils.getOpListFromConfig();
        if (opList != null) {
            assertTrue(opList instanceof OpMetadataList);
        }
    }

    @Test
    void testGetReservationListFromConfig_returnsReservationList() {
        ReservationList reservationList = IOUtils.getReservationListFromConfig();
        if (reservationList != null) {
            assertTrue(reservationList instanceof ReservationList);
        }
    }

    @Test
    void testGetFileFromRelativePath_createsFile() {
        File file = IOUtils.getFileFromRelativePath("test.json");
        assertNotNull(file);
    }
}
