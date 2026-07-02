package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpMetadataTest {

    private OpMetadata opMetadata;

    @BeforeEach
    void setUp() {
        opMetadata = new OpMetadata();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(opMetadata);
    }

    @Test
    void testParameterizedConstructor() {
        OpMetadata op = new OpMetadata("testOp", "Test operation", "void", 1);
        
        assertNotNull(op);
        assertEquals("testOp", op.getName());
        assertEquals("Test operation", op.getDescription());
        assertEquals("void", op.getType());
        assertEquals(1, op.getImpact());
    }

    @Test
    void testSetName() {
        opMetadata.setName("testName");
        assertEquals("testName", opMetadata.getName());
    }

    @Test
    void testSetDescription() {
        opMetadata.setDescription("Test description");
        assertEquals("Test description", opMetadata.getDescription());
    }

    @Test
    void testSetType() {
        opMetadata.setType("String");
        assertEquals("String", opMetadata.getType());
    }

    @Test
    void testSetImpact() {
        opMetadata.setImpact(2);
        assertEquals(2, opMetadata.getImpact());
    }

    @Test
    void testGetName() {
        opMetadata.setName("getName");
        assertEquals("getName", opMetadata.getName());
    }

    @Test
    void testGetDescription() {
        opMetadata.setDescription("Get description");
        assertEquals("Get description", opMetadata.getDescription());
    }

    @Test
    void testGetType() {
        opMetadata.setType("int");
        assertEquals("int", opMetadata.getType());
    }

    @Test
    void testGetImpact() {
        opMetadata.setImpact(3);
        assertEquals(3, opMetadata.getImpact());
    }

    @Test
    void testSetNullName() {
        opMetadata.setName(null);
        assertNull(opMetadata.getName());
    }

    @Test
    void testSetNullDescription() {
        opMetadata.setDescription(null);
        assertNull(opMetadata.getDescription());
    }

    @Test
    void testSetNullType() {
        opMetadata.setType(null);
        assertNull(opMetadata.getType());
    }

    @Test
    void testSetNegativeImpact() {
        opMetadata.setImpact(-1);
        assertEquals(-1, opMetadata.getImpact());
    }
}
