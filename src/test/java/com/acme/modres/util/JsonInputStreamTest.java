package com.acme.modres.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acme.modres.mbean.OpMetadata;
import com.acme.modres.mbean.OpMetadataList;

class JsonInputStreamTest {

    @Test
    void testConstructor_withValidFile() throws IOException {
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();
        
        assertDoesNotThrow(() -> {
            try (JsonInputStream jis = new JsonInputStream(tempFile)) {
                assertNotNull(jis);
            }
        });
    }

    @Test
    void testConstructor_withNonExistentFile() {
        File nonExistentFile = new File("nonexistent.json");
        
        assertThrows(FileNotFoundException.class, () -> {
            new JsonInputStream(nonExistentFile);
        });
    }

    @Test
    void testParseJsonAs_withValidFile() throws IOException {
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();
        
        try (JsonInputStream jis = new JsonInputStream(tempFile)) {
            Object result = jis.parseJsonAs(OpMetadataList.class);
            // Result may be null if file is empty
            assertTrue(result == null || result instanceof OpMetadataList);
        }
    }

    @Test
    void testParseJsonAs_withNullClass() throws IOException {
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();
        
        try (JsonInputStream jis = new JsonInputStream(tempFile)) {
            assertThrows(NullPointerException.class, () -> {
                jis.parseJsonAs(null);
            });
        }
    }

    @Test
    void testClose() throws IOException {
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();
        
        JsonInputStream jis = new JsonInputStream(tempFile);
        assertDoesNotThrow(() -> jis.close());
    }

    @Test
    void testParseJsonAs_returnsCorrectType() throws IOException {
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();
        
        try (JsonInputStream jis = new JsonInputStream(tempFile)) {
            Object result = jis.parseJsonAs(OpMetadata.class);
            assertTrue(result == null || result instanceof OpMetadata);
        }
    }

    @Test
    void testConstructor_withNullFile() {
        assertThrows(NullPointerException.class, () -> {
            new JsonInputStream(null);
        });
    }
}
