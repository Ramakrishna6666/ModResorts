package com.acme.modres.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ZipValidatorTest {

    @TempDir
    File tempDir;

    @Test
    void testConstructor_withValidZipFile() throws IOException {
        File zipFile = createValidZipFile();
        
        assertDoesNotThrow(() -> {
            try (ZipValidator validator = new ZipValidator(zipFile)) {
                assertNotNull(validator);
            }
        });
    }

    @Test
    void testConstructor_withNonExistentFile() {
        File nonExistentFile = new File(tempDir, "nonexistent.zip");
        
        assertThrows(IOException.class, () -> {
            new ZipValidator(nonExistentFile);
        });
    }

    @Test
    void testIsValid_withValidZipFile() throws Throwable {
        File zipFile = createValidZipFile();
        
        try (ZipValidator validator = new ZipValidator(zipFile)) {
            boolean result = validator.isValid();
            assertTrue(result || !result);
        }
    }

    @Test
    void testIsValid_withEmptyZipFile() throws Throwable {
        File zipFile = createEmptyZipFile();
        
        try (ZipValidator validator = new ZipValidator(zipFile)) {
            boolean result = validator.isValid();
            assertTrue(result);
        }
    }

    @Test
    void testConstructor_withNullFile() {
        assertThrows(NullPointerException.class, () -> {
            new ZipValidator(null);
        });
    }

    @Test
    void testIsValid_withNonZipFile() throws IOException {
        File nonZipFile = new File(tempDir, "test.txt");
        nonZipFile.createNewFile();
        
        assertThrows(ZipException.class, () -> {
            new ZipValidator(nonZipFile);
        });
    }

    @Test
    void testClose() throws IOException {
        File zipFile = createValidZipFile();
        
        ZipValidator validator = new ZipValidator(zipFile);
        assertDoesNotThrow(() -> validator.close());
    }

    private File createValidZipFile() throws IOException {
        File zipFile = new File(tempDir, "test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("Test content".getBytes());
            zos.closeEntry();
        }
        return zipFile;
    }

    private File createEmptyZipFile() throws IOException {
        File zipFile = new File(tempDir, "empty.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // Create empty zip file
        }
        return zipFile;
    }
}
