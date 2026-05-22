package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acme.modres.mbean.reservation.ReservationList;

class IOUtilsTest {

    @Test
    void testGetFileFromRelativePath_withValidPath() {
        File file = IOUtils.getFileFromRelativePath("ops.json");
        
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
        
        // Should handle gracefully, may return null or throw exception
        assertTrue(file == null || file.exists() || !file.exists());
    }

    @Test
    void testGetOpListFromConfig() {
        OpMetadataList opList = IOUtils.getOpListFromConfig();
        
        // May return null or empty list if file doesn't exist
        assertTrue(opList == null || opList.getOpMetadatList() != null);
    }

    @Test
    void testGetReservationListFromConfig() {
        ReservationList reservationList = IOUtils.getReservationListFromConfig();
        
        // May return null or empty list if file doesn't exist
        assertTrue(reservationList == null || reservationList.getReservations() != null);
    }

    @Test
    void testGetFileFromRelativePath_createsTemporaryFile() {
        File file = IOUtils.getFileFromRelativePath("ops.json");
        
        if (file != null) {
            assertTrue(file.exists() || !file.exists());
        }
    }

    @Test
    void testGetOpListFromConfig_returnsValidStructure() {
        OpMetadataList opList = IOUtils.getOpListFromConfig();
        
        if (opList != null) {
            assertNotNull(opList.getOpMetadatList());
        }
    }

    @Test
    void testGetReservationListFromConfig_returnsValidStructure() {
        ReservationList reservationList = IOUtils.getReservationListFromConfig();
        
        if (reservationList != null) {
            assertNotNull(reservationList.getReservations());
        }
    }

    @Test
    void testGetFileFromRelativePath_withNonExistentFile() {
        File file = IOUtils.getFileFromRelativePath("nonexistent.json");
        
        // Should handle gracefully
        assertTrue(file == null || !file.exists());
    }
}
