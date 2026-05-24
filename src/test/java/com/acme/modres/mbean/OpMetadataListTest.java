package com.acme.modres.mbean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpMetadataListTest {

    private OpMetadataList opMetadataList;

    @BeforeEach
    void setUp() {
        opMetadataList = new OpMetadataList();
    }

    @Test
    void testConstructor_shouldCreateEmptyList() {
        assertNotNull(opMetadataList);
        assertNotNull(opMetadataList.getOpMetadatList());
        assertTrue(opMetadataList.getOpMetadatList().isEmpty());
    }

    @Test
    void testAdd_shouldAddOpMetadata() {
        OpMetadata metadata = new OpMetadata("test", "description", "type", 1);
        opMetadataList.add(metadata);
        assertEquals(1, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testAdd_multipleItems_shouldAddAll() {
        OpMetadata metadata1 = new OpMetadata("test1", "desc1", "type1", 1);
        OpMetadata metadata2 = new OpMetadata("test2", "desc2", "type2", 2);
        opMetadataList.add(metadata1);
        opMetadataList.add(metadata2);
        assertEquals(2, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testGetOpMetadatList_shouldReturnList() {
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        assertNotNull(list);
    }

    @Test
    void testSetOpMetadatList_shouldSetList() {
        List<OpMetadata> newList = new ArrayList<>();
        newList.add(new OpMetadata("test", "desc", "type", 1));
        opMetadataList.setOpMetadatList(newList);
        assertEquals(1, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testSetOpMetadatList_withNull_shouldSetNull() {
        opMetadataList.setOpMetadatList(null);
        assertNull(opMetadataList.getOpMetadatList());
    }

    @Test
    void testAdd_withNullMetadata_shouldAddNull() {
        opMetadataList.add(null);
        assertEquals(1, opMetadataList.getOpMetadatList().size());
        assertNull(opMetadataList.getOpMetadatList().get(0));
    }

    @Test
    void testGetOpMetadatList_afterMultipleAdds_shouldReturnCorrectSize() {
        for (int i = 0; i < 5; i++) {
            opMetadataList.add(new OpMetadata("test" + i, "desc", "type", i));
        }
        assertEquals(5, opMetadataList.getOpMetadatList().size());
    }
}
