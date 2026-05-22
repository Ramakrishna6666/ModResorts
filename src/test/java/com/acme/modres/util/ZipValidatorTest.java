package com.acme.modres.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZipValidatorTest {

    private File tempZipFile;

    @BeforeEach
    void setUp() throws IOException {
        tempZipFile = File.createTempFile("test", ".zip");
    }

    @AfterEach
    void tearDown() {
        if (tempZipFile != null && tempZipFile.exists()) {
            tempZipFile.delete();
        }
    }

    @Test
    void testConstructor_withValidZipFile() throws IOException {
        createValidZipFile(tempZipFile);
        
        ZipValidator validator = new ZipValidator(tempZipFile);
        
        assertNotNull(validator);
        validator.close();
    }

    @Test
    void testConstructor_withInvalidZipFile() throws IOException {
        // Create a non-zip file
        FileOutputStream fos = new FileOutputStream(tempZipFile);
        fos.write("not a zip file".getBytes());
        fos.close();
        
        assertThrows(ZipException.class, () -> {
            new ZipValidator(tempZipFile);
        });
    }

    @Test
    void testConstructor_withNonExistentFile() {
        File nonExistent = new File("nonexistent.zip");
        
        assertThrows(IOException.class, () -> {
            new ZipValidator(nonExistent);
        });
    }

    @Test
    void testIsValid_withEmptyZipFile() throws Throwable {
        createEmptyZipFile(tempZipFile);
        
        ZipValidator validator = new ZipValidator(tempZipFile);
        boolean result = validator.isValid();
        
        assertTrue(result);
        validator.close();
    }

    @Test
    void testIsValid_withValidZipFile() throws Throwable {
        createValidZipFile(tempZipFile);
        
        ZipValidator validator = new ZipValidator(tempZipFile);
        boolean result = validator.isValid();
        
        // Should return false for non-empty zip
        assertFalse(result);
        validator.close();
    }

    @Test
    void testIsValid_withMultipleEntries() throws Throwable {
        createZipFileWithMultipleEntries(tempZipFile);
        
        ZipValidator validator = new ZipValidator(tempZipFile);
        boolean result = validator.isValid();
        
        assertFalse(result);
        validator.close();
    }

    @Test
    void testClose_closesZipFile() throws IOException {
        createValidZipFile(tempZipFile);
        
        ZipValidator validator = new ZipValidator(tempZipFile);
        validator.close();
        
        assertThrows(IllegalStateException.class, () -> {
            validator.entries();
        });
    }

    @Test
    void testEntries_returnsEnumeration() throws IOException {
        createValidZipFile(tempZipFile);
        
        ZipValidator validator = new ZipValidator(tempZipFile);
        
        assertNotNull(validator.entries());
        validator.close();
    }

    @Test
    void testIsValid_withNonExistentFile() throws IOException {
        createValidZipFile(tempZipFile);
        ZipValidator validator = new ZipValidator(tempZipFile);
        validator.close();
        
        tempZipFile.delete();
        
        assertThrows(Throwable.class, () -> {
            validator.isValid();
        });
    }

    // Helper methods
    private void createEmptyZipFile(File file) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            // Create empty zip file
        }
    }

    private void createValidZipFile(File file) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("test content".getBytes());
            zos.closeEntry();
        }
    }

    private void createZipFileWithMultipleEntries(File file) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            for (int i = 0; i < 3; i++) {
                ZipEntry entry = new ZipEntry("file" + i + ".txt");
                zos.putNextEntry(entry);
                zos.write(("content " + i).getBytes());
                zos.closeEntry();
            }
        }
    }
}
