package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CustomPermissionTest {

    @Test
    void testConstructor_withName() {
        CustomPermission permission = new CustomPermission("testPermission");
        assertNotNull(permission);
    }

    @Test
    void testConstructor_withNameAndActions() {
        CustomPermission permission = new CustomPermission("testPermission", "read,write");
        assertNotNull(permission);
    }

    @Test
    void testGetName() {
        CustomPermission permission = new CustomPermission("testPermission");
        assertEquals("testPermission", permission.getName());
    }

    @Test
    void testGetActions() {
        CustomPermission permission = new CustomPermission("testPermission", "read,write");
        assertEquals("", permission.getActions());
    }

    @Test
    void testConstructor_withNullName() {
        assertThrows(NullPointerException.class, () -> {
            new CustomPermission(null);
        });
    }

    @Test
    void testConstructor_withEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CustomPermission("");
        });
    }

    @Test
    void testConstructor_withNullActions() {
        CustomPermission permission = new CustomPermission("testPermission", null);
        assertNotNull(permission);
    }

    @Test
    void testImplies() {
        CustomPermission permission1 = new CustomPermission("test.*");
        CustomPermission permission2 = new CustomPermission("test.read");
        
        assertTrue(permission1.implies(permission2));
    }

    @Test
    void testEquals() {
        CustomPermission permission1 = new CustomPermission("testPermission");
        CustomPermission permission2 = new CustomPermission("testPermission");
        
        assertEquals(permission1, permission2);
    }

    @Test
    void testHashCode() {
        CustomPermission permission1 = new CustomPermission("testPermission");
        CustomPermission permission2 = new CustomPermission("testPermission");
        
        assertEquals(permission1.hashCode(), permission2.hashCode());
    }
}
