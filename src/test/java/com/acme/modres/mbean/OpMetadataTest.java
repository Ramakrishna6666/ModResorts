package com.acme.modres.mbean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class OpMetadataTest {

    private OpMetadata opMetadata;

    @BeforeEach
    void setUp() {
        opMetadata = new OpMetadata();
    }

    @Test
    void testDefaultConstructor_shouldCreateInstance() {
        assertNotNull(opMetadata);
    }

    @Test
    void testParameterizedConstructor_shouldSetAllFields() {
        OpMetadata metadata = new OpMetadata("testName", "testDesc", "testType", 5);
        assertEquals("testName", metadata.getName());
        assertEquals("testDesc", metadata.getDescription());
        assertEquals("testType", metadata.getType());
        assertEquals(5, metadata.getImpact());
    }

    @Test
    void testSetName_shouldSetName() {
        opMetadata.setName("newName");
        assertEquals("newName", opMetadata.getName());
    }

    @Test
    void testSetDescription_shouldSetDescription() {
        opMetadata.setDescription("newDescription");
        assertEquals("newDescription", opMetadata.getDescription());
    }

    @Test
    void testSetType_shouldSetType() {
        opMetadata.setType("newType");
        assertEquals("newType", opMetadata.getType());
    }

    @Test
    void testSetImpact_shouldSetImpact() {
        opMetadata.setImpact(10);
        assertEquals(10, opMetadata.getImpact());
    }

    @Test
    void testGetName_withNullValue_shouldReturnNull() {
        assertNull(opMetadata.getName());
    }

    @Test
    void testGetDescription_withNullValue_shouldReturnNull() {
        assertNull(opMetadata.getDescription());
    }

    @Test
    void testGetType_withNullValue_shouldReturnNull() {
        assertNull(opMetadata.getType());
    }

    @Test
    void testGetImpact_withDefaultValue_shouldReturnZero() {
        assertEquals(0, opMetadata.getImpact());
    }

    @Test
    void testSetName_withNull_shouldSetNull() {
        opMetadata.setName(null);
        assertNull(opMetadata.getName());
    }

    @Test
    void testSetImpact_withNegativeValue_shouldSetNegative() {
        opMetadata.setImpact(-5);
        assertEquals(-5, opMetadata.getImpact());
    }
}
