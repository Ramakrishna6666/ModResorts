package com.acme.modres.mbean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import javax.management.MBeanOperationInfo;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DMBeanUtilsTest {

    private OpMetadataList opMetadataList;

    @BeforeEach
    void setUp() {
        opMetadataList = new OpMetadataList();
    }

    @Test
    void testGetOps_withNullOpList_shouldReturnNull() {
        MBeanOperationInfo[] result = DMBeanUtils.getOps(null);
        assertNull(result);
    }

    @Test
    void testGetOps_withEmptyOpList_shouldReturnNull() {
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opMetadataList);
        assertNull(result);
    }

    @Test
    void testGetOps_withValidOpList_shouldReturnOperations() {
        opMetadataList.add(new OpMetadata("testOp", "Test Operation", "void", 1));
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opMetadataList);
        assertNotNull(result);
        assertEquals(1, result.length);
    }

    @Test
    void testGetOps_withMultipleOperations_shouldReturnAll() {
        opMetadataList.add(new OpMetadata("op1", "Operation 1", "void", 1));
        opMetadataList.add(new OpMetadata("op2", "Operation 2", "String", 2));
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opMetadataList);
        assertNotNull(result);
        assertEquals(2, result.length);
    }

    @Test
    void testGetOps_shouldSetCorrectOperationName() {
        opMetadataList.add(new OpMetadata("testOperation", "desc", "void", 1));
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opMetadataList);
        assertEquals("testOperation", result[0].getName());
    }

    @Test
    void testGetOps_shouldSetCorrectDescription() {
        opMetadataList.add(new OpMetadata("op", "Test Description", "void", 1));
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opMetadataList);
        assertEquals("Test Description", result[0].getDescription());
    }

    @Test
    void testGetOps_shouldSetCorrectReturnType() {
        opMetadataList.add(new OpMetadata("op", "desc", "java.lang.String", 1));
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opMetadataList);
        assertEquals("java.lang.String", result[0].getReturnType());
    }

    @Test
    void testGetOps_shouldSetCorrectImpact() {
        opMetadataList.add(new OpMetadata("op", "desc", "void", 3));
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opMetadataList);
        assertEquals(3, result[0].getImpact());
    }

    @Test
    void testGetOps_withNullMetadataList_shouldReturnNull() {
        OpMetadataList nullList = new OpMetadataList();
        nullList.setOpMetadatList(null);
        MBeanOperationInfo[] result = DMBeanUtils.getOps(nullList);
        assertNull(result);
    }
}
