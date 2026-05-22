package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CustomPermissionTest {

    @Test
    void testConstructor_withName() {
        CustomPermission permission = new CustomPermission("testPermission");
        
        assertNotNull(permission);
        assertEquals("testPermission", permission.getName());
    }

    @Test
    void testConstructor_withNameAndActions() {
        CustomPermission permission = new CustomPermission("testPermission", "read,write");
        
        assertNotNull(permission);
        assertEquals("testPermission", permission.getName());
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
        assertEquals("testPermission", permission.getName());
    }

    @Test
    void testGetName() {
        CustomPermission permission = new CustomPermission("myPermission");
        
        assertEquals("myPermission", permission.getName());
    }

    @Test
    void testGetActions() {
        CustomPermission permission = new CustomPermission("testPermission", "read");
        
        assertEquals("", permission.getActions());
    }

    @Test
    void testImplies_samePermission() {
        CustomPermission permission1 = new CustomPermission("testPermission");
        CustomPermission permission2 = new CustomPermission("testPermission");
        
        assertTrue(permission1.implies(permission2));
    }

    @Test
    void testImplies_differentPermission() {
        CustomPermission permission1 = new CustomPermission("permission1");
        CustomPermission permission2 = new CustomPermission("permission2");
        
        assertFalse(permission1.implies(permission2));
    }

    @Test
    void testImplies_withWildcard() {
        CustomPermission permission1 = new CustomPermission("test.*");
        CustomPermission permission2 = new CustomPermission("test.read");
        
        assertTrue(permission1.implies(permission2));
    }

    @Test
    void testEquals_samePermission() {
        CustomPermission permission1 = new CustomPermission("testPermission");
        CustomPermission permission2 = new CustomPermission("testPermission");
        
        assertEquals(permission1, permission2);
    }

    @Test
    void testEquals_differentPermission() {
        CustomPermission permission1 = new CustomPermission("permission1");
        CustomPermission permission2 = new CustomPermission("permission2");
        
        assertNotEquals(permission1, permission2);
    }

    @Test
    void testHashCode_samePermission() {
        CustomPermission permission1 = new CustomPermission("testPermission");
        CustomPermission permission2 = new CustomPermission("testPermission");
        
        assertEquals(permission1.hashCode(), permission2.hashCode());
    }

    @Test
    void testConstructor_withComplexName() {
        CustomPermission permission = new CustomPermission("com.acme.modres.security.CustomPermission");
        
        assertNotNull(permission);
        assertEquals("com.acme.modres.security.CustomPermission", permission.getName());
    }
}
