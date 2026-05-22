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
        assertTrue(opMetadataList.getOpMetadatList().isEmpty());
    }

    @Test
    void testAdd_singleElement() {
        OpMetadata metadata = new OpMetadata("op1", "desc1", "void", 1);
        opMetadataList.add(metadata);
        
        assertEquals(1, opMetadataList.getOpMetadatList().size());
        assertEquals("op1", opMetadataList.getOpMetadatList().get(0).getName());
    }

    @Test
    void testAdd_multipleElements() {
        OpMetadata metadata1 = new OpMetadata("op1", "desc1", "void", 1);
        OpMetadata metadata2 = new OpMetadata("op2", "desc2", "String", 2);
        OpMetadata metadata3 = new OpMetadata("op3", "desc3", "int", 3);
        
        opMetadataList.add(metadata1);
        opMetadataList.add(metadata2);
        opMetadataList.add(metadata3);
        
        assertEquals(3, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testGetOpMetadatList() {
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testSetOpMetadatList() {
        List<OpMetadata> newList = new ArrayList<>();
        newList.add(new OpMetadata("op1", "desc1", "void", 1));
        newList.add(new OpMetadata("op2", "desc2", "String", 2));
        
        opMetadataList.setOpMetadatList(newList);
        
        assertEquals(2, opMetadataList.getOpMetadatList().size());
        assertEquals("op1", opMetadataList.getOpMetadatList().get(0).getName());
        assertEquals("op2", opMetadataList.getOpMetadatList().get(1).getName());
    }

    @Test
    void testSetOpMetadatList_withEmptyList() {
        List<OpMetadata> emptyList = new ArrayList<>();
        opMetadataList.setOpMetadatList(emptyList);
        
        assertTrue(opMetadataList.getOpMetadatList().isEmpty());
    }

    @Test
    void testSetOpMetadatList_withNull() {
        opMetadataList.setOpMetadatList(null);
        
        assertNull(opMetadataList.getOpMetadatList());
    }

    @Test
    void testAdd_afterSetOpMetadatList() {
        List<OpMetadata> newList = new ArrayList<>();
        newList.add(new OpMetadata("op1", "desc1", "void", 1));
        opMetadataList.setOpMetadatList(newList);
        
        OpMetadata metadata = new OpMetadata("op2", "desc2", "String", 2);
        opMetadataList.add(metadata);
        
        assertEquals(2, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testAdd_nullElement() {
        opMetadataList.add(null);
        
        assertEquals(1, opMetadataList.getOpMetadatList().size());
        assertNull(opMetadataList.getOpMetadatList().get(0));
    }

    @Test
    void testGetOpMetadatList_returnsModifiableList() {
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        list.add(new OpMetadata("op1", "desc1", "void", 1));
        
        assertEquals(1, opMetadataList.getOpMetadatList().size());
    }
}
