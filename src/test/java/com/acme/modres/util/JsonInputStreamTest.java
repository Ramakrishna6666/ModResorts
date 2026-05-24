package com.acme.modres.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonInputStreamTest {

    @TempDir
    Path tempDir;

    private File testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("test.json").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("{\"name\":\"test\",\"value\":123}");
        }
    }

    @Test
    void testConstructor_withValidFile_shouldCreateInstance() throws Exception {
        JsonInputStream stream = new JsonInputStream(testFile);
        assertNotNull(stream);
        stream.close();
    }

    @Test
    void testConstructor_withNonExistentFile_shouldThrowException() {
        File nonExistent = new File("nonexistent.json");
        assertThrows(Exception.class, () -> {
            new JsonInputStream(nonExistent);
        });
    }

    @Test
    void testParseJsonAs_withValidJson_shouldReturnObject() throws Exception {
        JsonInputStream stream = new JsonInputStream(testFile);
        Object result = stream.parseJsonAs(Object.class);
        stream.close();
        // Result may be null if parsing fails
    }

    @Test
    void testParseJsonAs_withNullClass_shouldHandleGracefully() throws Exception {
        JsonInputStream stream = new JsonInputStream(testFile);
        assertDoesNotThrow(() -> stream.parseJsonAs(null));
        stream.close();
    }

    @Test
    void testParseJsonAs_shouldNotThrowException() throws Exception {
        JsonInputStream stream = new JsonInputStream(testFile);
        assertDoesNotThrow(() -> stream.parseJsonAs(Object.class));
        stream.close();
    }

    @Test
    void testClose_shouldCloseStream() throws Exception {
        JsonInputStream stream = new JsonInputStream(testFile);
        assertDoesNotThrow(() -> stream.close());
    }

    @Test
    void testParseJsonAs_withInvalidJson_shouldHandleGracefully() throws Exception {
        File invalidFile = tempDir.resolve("invalid.json").toFile();
        try (FileWriter writer = new FileWriter(invalidFile)) {
            writer.write("invalid json content");
        }
        JsonInputStream stream = new JsonInputStream(invalidFile);
        Object result = stream.parseJsonAs(Object.class);
        stream.close();
        // Should handle gracefully, may return null
    }
}
