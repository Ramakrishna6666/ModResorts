package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import javax.management.MBeanOperationInfo;

import org.junit.jupiter.api.Test;

class DMBeanUtilsTest {

    @Test
    void testGetOps_withNullOpList() {
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(null);
        assertNull(ops);
    }

    @Test
    void testGetOps_withEmptyOpList() {
        OpMetadataList opList = new OpMetadataList();
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        assertNotNull(ops);
        assertEquals(0, ops.length);
    }

    @Test
    void testGetOps_withSingleOp() {
        OpMetadataList opList = new OpMetadataList();
        OpMetadata opMetadata = new OpMetadata("testOp", "Test operation", "void", 1);
        opList.add(opMetadata);
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNotNull(ops);
        assertEquals(1, ops.length);
        assertEquals("testOp", ops[0].getName());
    }

    @Test
    void testGetOps_withMultipleOps() {
        OpMetadataList opList = new OpMetadataList();
        opList.add(new OpMetadata("op1", "Operation 1", "void", 1));
        opList.add(new OpMetadata("op2", "Operation 2", "String", 2));
        opList.add(new OpMetadata("op3", "Operation 3", "int", 3));
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNotNull(ops);
        assertEquals(3, ops.length);
    }

    @Test
    void testGetOps_withNullMetadataList() {
        OpMetadataList opList = new OpMetadataList();
        opList.setOpMetadatList(null);
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNull(ops);
    }

    @Test
    void testGetOps_preservesOperationDetails() {
        OpMetadataList opList = new OpMetadataList();
        OpMetadata opMetadata = new OpMetadata("testOp", "Test description", "String", 2);
        opList.add(opMetadata);
        
        MBeanOperationInfo[] ops = DMBeanUtils.getOps(opList);
        
        assertNotNull(ops);
        assertEquals(1, ops.length);
        assertEquals("testOp", ops[0].getName());
        assertEquals("Test description", ops[0].getDescription());
        assertEquals("String", ops[0].getReturnType());
    }
}
