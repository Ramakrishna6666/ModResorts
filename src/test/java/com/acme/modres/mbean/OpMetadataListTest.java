package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpMetadataListTest {

    private OpMetadataList opMetadataList;

    @BeforeEach
    void setUp() {
        opMetadataList = new OpMetadataList();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(opMetadataList);
        assertNotNull(opMetadataList.getOpMetadatList());
    }

    @Test
    void testAdd() {
        OpMetadata opMetadata = new OpMetadata("testOp", "Test", "void", 1);
        opMetadataList.add(opMetadata);
        
        assertEquals(1, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testAddMultiple() {
        opMetadataList.add(new OpMetadata("op1", "Op 1", "void", 1));
        opMetadataList.add(new OpMetadata("op2", "Op 2", "String", 2));
        opMetadataList.add(new OpMetadata("op3", "Op 3", "int", 3));
        
        assertEquals(3, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testGetOpMetadatList() {
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        assertNotNull(list);
        assertTrue(list instanceof ArrayList);
    }

    @Test
    void testSetOpMetadatList() {
        List<OpMetadata> newList = new ArrayList<>();
        newList.add(new OpMetadata("op1", "Op 1", "void", 1));
        
        opMetadataList.setOpMetadatList(newList);
        
        assertEquals(1, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testAddNull() {
        opMetadataList.add(null);
        assertEquals(1, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testSetOpMetadatList_withNull() {
        opMetadataList.setOpMetadatList(null);
        assertNull(opMetadataList.getOpMetadatList());
    }

    @Test
    void testGetOpMetadatList_isEmpty() {
        assertTrue(opMetadataList.getOpMetadatList().isEmpty());
    }

    @Test
    void testAdd_maintainsOrder() {
        OpMetadata op1 = new OpMetadata("op1", "Op 1", "void", 1);
        OpMetadata op2 = new OpMetadata("op2", "Op 2", "String", 2);
        
        opMetadataList.add(op1);
        opMetadataList.add(op2);
        
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        assertEquals("op1", list.get(0).getName());
        assertEquals("op2", list.get(1).getName());
    }
}
