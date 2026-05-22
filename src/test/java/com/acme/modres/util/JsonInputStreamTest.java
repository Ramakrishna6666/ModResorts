package com.acme.modres.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonInputStreamTest {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".json");
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testConstructor_withValidFile() throws FileNotFoundException {
        JsonInputStream stream = new JsonInputStream(tempFile);
        
        assertNotNull(stream);
        assertDoesNotThrow(() -> stream.close());
    }

    @Test
    void testConstructor_withNonExistentFile() {
        File nonExistent = new File("nonexistent.json");
        
        assertThrows(FileNotFoundException.class, () -> {
            new JsonInputStream(nonExistent);
        });
    }

    @Test
    void testParseJsonAs_withValidJson() throws IOException {
        FileWriter writer = new FileWriter(tempFile);
        writer.write("{\"name\":\"test\",\"value\":123}");
        writer.close();
        
        JsonInputStream stream = new JsonInputStream(tempFile);
        Object result = stream.parseJsonAs(TestClass.class);
        
        assertNotNull(result);
        stream.close();
    }

    @Test
    void testParseJsonAs_withEmptyFile() throws IOException {
        JsonInputStream stream = new JsonInputStream(tempFile);
        Object result = stream.parseJsonAs(TestClass.class);
        
        // May return null for empty file
        assertTrue(result == null || result instanceof TestClass);
        stream.close();
    }

    @Test
    void testParseJsonAs_withInvalidJson() throws IOException {
        FileWriter writer = new FileWriter(tempFile);
        writer.write("invalid json content");
        writer.close();
        
        JsonInputStream stream = new JsonInputStream(tempFile);
        Object result = stream.parseJsonAs(TestClass.class);
        
        // Should handle gracefully
        assertTrue(result == null || result instanceof TestClass);
        stream.close();
    }

    @Test
    void testParseJsonAs_withNullClass() throws IOException {
        JsonInputStream stream = new JsonInputStream(tempFile);
        
        assertThrows(NullPointerException.class, () -> {
            stream.parseJsonAs(null);
        });
        
        stream.close();
    }

    @Test
    void testClose_closesStream() throws IOException {
        JsonInputStream stream = new JsonInputStream(tempFile);
        stream.close();
        
        assertThrows(IOException.class, () -> {
            stream.read();
        });
    }

    @Test
    void testParseJsonAs_withComplexObject() throws IOException {
        FileWriter writer = new FileWriter(tempFile);
        writer.write("{\"name\":\"test\",\"value\":123,\"nested\":{\"key\":\"value\"}}");
        writer.close();
        
        JsonInputStream stream = new JsonInputStream(tempFile);
        Object result = stream.parseJsonAs(TestClass.class);
        
        assertNotNull(result);
        stream.close();
    }

    @Test
    void testParseJsonAs_withArray() throws IOException {
        FileWriter writer = new FileWriter(tempFile);
        writer.write("[{\"name\":\"test1\"},{\"name\":\"test2\"}]");
        writer.close();
        
        JsonInputStream stream = new JsonInputStream(tempFile);
        Object result = stream.parseJsonAs(TestClass[].class);
        
        assertTrue(result == null || result instanceof TestClass[]);
        stream.close();
    }

    // Helper test class
    static class TestClass {
        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
