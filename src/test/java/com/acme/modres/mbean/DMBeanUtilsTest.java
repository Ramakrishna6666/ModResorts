package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import javax.management.MBeanOperationInfo;

import org.junit.jupiter.api.Test;

class DMBeanUtilsTest {

    @Test
    void testGetOps_withValidOpList() {
        OpMetadataList opList = new OpMetadataList();
        opList.add(new OpMetadata("operation1", "Description 1", "void", 1));
        opList.add(new OpMetadata("operation2", "Description 2", "String", 2));
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNotNull(ops);
        assertEquals(2, ops.length);
        assertEquals("operation1", ops[0].getName());
        assertEquals("Description 1", ops[0].getDescription());
        assertEquals("operation2", ops[1].getName());
        assertEquals("Description 2", ops[1].getDescription());
    }

    @Test
    void testGetOps_withNullOpList() {
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(null);
        
        assertNull(ops);
    }

    @Test
    void testGetOps_withEmptyOpList() {
        OpMetadataList opList = new OpMetadataList();
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNull(ops);
    }

    @Test
    void testGetOps_withSingleOperation() {
        OpMetadataList opList = new OpMetadataList();
        opList.add(new OpMetadata("singleOp", "Single operation", "int", 0));
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNotNull(ops);
        assertEquals(1, ops.length);
        assertEquals("singleOp", ops[0].getName());
    }

    @Test
    void testGetOps_withMultipleOperations() {
        OpMetadataList opList = new OpMetadataList();
        for (int i = 0; i < 5; i++) {
            opList.add(new OpMetadata("op" + i, "Description " + i, "void", i));
        }
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNotNull(ops);
        assertEquals(5, ops.length);
    }

    @Test
    void testGetOps_withNullMetadataList() {
        OpMetadataList opList = new OpMetadataList();
        opList.setOpMetadatList(null);
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNull(ops);
    }

    @Test
    void testGetOps_verifyOperationDetails() {
        OpMetadataList opList = new OpMetadataList();
        opList.add(new OpMetadata("testOp", "Test Description", "boolean", 3));
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNotNull(ops);
        assertEquals(1, ops.length);
        assertEquals("testOp", ops[0].getName());
        assertEquals("Test Description", ops[0].getDescription());
        assertEquals("boolean", ops[0].getReturnType());
        assertEquals(3, ops[0].getImpact());
    }
}
