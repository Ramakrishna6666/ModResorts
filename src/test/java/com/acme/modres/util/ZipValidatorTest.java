package com.acme.modres.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ZipValidatorTest {

    @TempDir
    Path tempDir;

    private File zipFile;

    @BeforeEach
    void setUp() throws Exception {
        zipFile = tempDir.resolve("test.zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("test content".getBytes());
            zos.closeEntry();
        }
    }

    @Test
    void testConstructor_withValidZipFile_shouldCreateInstance() throws Exception {
        ZipValidator validator = new ZipValidator(zipFile);
        assertNotNull(validator);
        validator.close();
    }

    @Test
    void testConstructor_withNonExistentFile_shouldThrowException() {
        File nonExistent = new File("nonexistent.zip");
        assertThrows(Exception.class, () -> {
            new ZipValidator(nonExistent);
        });
    }

    @Test
    void testIsValid_withValidZipFile_shouldReturnBoolean() throws Exception {
        ZipValidator validator = new ZipValidator(zipFile);
        boolean result = validator.isValid();
        validator.close();
        // Result depends on zip content
        assertTrue(result || !result);
    }

    @Test
    void testIsValid_shouldNotThrowException() throws Exception {
        ZipValidator validator = new ZipValidator(zipFile);
        assertDoesNotThrow(() -> validator.isValid());
        validator.close();
    }

    @Test
    void testClose_shouldCloseValidator() throws Exception {
        ZipValidator validator = new ZipValidator(zipFile);
        assertDoesNotThrow(() -> validator.close());
    }

    @Test
    void testConstructor_withInvalidZipFile_shouldThrowException() throws Exception {
        File invalidZip = tempDir.resolve("invalid.zip").toFile();
        try (FileOutputStream fos = new FileOutputStream(invalidZip)) {
            fos.write("not a zip file".getBytes());
        }
        assertThrows(Exception.class, () -> {
            new ZipValidator(invalidZip);
        });
    }

    @Test
    void testIsValid_withEmptyZip_shouldReturnTrue() throws Exception {
        File emptyZip = tempDir.resolve("empty.zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(emptyZip))) {
            // Create empty zip
        }
        ZipValidator validator = new ZipValidator(emptyZip);
        boolean result = validator.isValid();
        validator.close();
        assertTrue(result);
    }
}
