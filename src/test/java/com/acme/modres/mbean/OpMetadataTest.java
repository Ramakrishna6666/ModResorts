package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OpMetadataTest {

    @Test
    void testDefaultConstructor() {
        OpMetadata metadata = new OpMetadata();
        
        assertNotNull(metadata);
        assertNull(metadata.getName());
        assertNull(metadata.getDescription());
        assertNull(metadata.getType());
        assertEquals(0, metadata.getImpact());
    }

    @Test
    void testParameterizedConstructor() {
        OpMetadata metadata = new OpMetadata("testOp", "Test Description", "String", 2);
        
        assertNotNull(metadata);
        assertEquals("testOp", metadata.getName());
        assertEquals("Test Description", metadata.getDescription());
        assertEquals("String", metadata.getType());
        assertEquals(2, metadata.getImpact());
    }

    @Test
    void testSetName() {
        OpMetadata metadata = new OpMetadata();
        metadata.setName("newName");
        
        assertEquals("newName", metadata.getName());
    }

    @Test
    void testSetDescription() {
        OpMetadata metadata = new OpMetadata();
        metadata.setDescription("New Description");
        
        assertEquals("New Description", metadata.getDescription());
    }

    @Test
    void testSetType() {
        OpMetadata metadata = new OpMetadata();
        metadata.setType("void");
        
        assertEquals("void", metadata.getType());
    }

    @Test
    void testSetImpact() {
        OpMetadata metadata = new OpMetadata();
        metadata.setImpact(3);
        
        assertEquals(3, metadata.getImpact());
    }

    @Test
    void testGetName() {
        OpMetadata metadata = new OpMetadata("operation", "desc", "type", 1);
        
        assertEquals("operation", metadata.getName());
    }

    @Test
    void testGetDescription() {
        OpMetadata metadata = new OpMetadata("operation", "desc", "type", 1);
        
        assertEquals("desc", metadata.getDescription());
    }

    @Test
    void testGetType() {
        OpMetadata metadata = new OpMetadata("operation", "desc", "int", 1);
        
        assertEquals("int", metadata.getType());
    }

    @Test
    void testGetImpact() {
        OpMetadata metadata = new OpMetadata("operation", "desc", "type", 5);
        
        assertEquals(5, metadata.getImpact());
    }

    @Test
    void testSettersWithNullValues() {
        OpMetadata metadata = new OpMetadata();
        metadata.setName(null);
        metadata.setDescription(null);
        metadata.setType(null);
        
        assertNull(metadata.getName());
        assertNull(metadata.getDescription());
        assertNull(metadata.getType());
    }

    @Test
    void testSettersWithEmptyStrings() {
        OpMetadata metadata = new OpMetadata();
        metadata.setName("");
        metadata.setDescription("");
        metadata.setType("");
        
        assertEquals("", metadata.getName());
        assertEquals("", metadata.getDescription());
        assertEquals("", metadata.getType());
    }

    @Test
    void testImpactWithNegativeValue() {
        OpMetadata metadata = new OpMetadata();
        metadata.setImpact(-1);
        
        assertEquals(-1, metadata.getImpact());
    }

    @Test
    void testImpactWithZeroValue() {
        OpMetadata metadata = new OpMetadata();
        metadata.setImpact(0);
        
        assertEquals(0, metadata.getImpact());
    }
}
