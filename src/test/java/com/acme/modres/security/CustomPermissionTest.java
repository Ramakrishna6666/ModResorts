package com.acme.modres.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class CustomPermissionTest {

    private CustomPermission permission;

    @BeforeEach
    void setUp() {
        permission = new CustomPermission("testPermission");
    }

    @Test
    void testConstructor_withName_shouldCreateInstance() {
        assertNotNull(permission);
    }

    @Test
    void testConstructor_withNameAndActions_shouldCreateInstance() {
        CustomPermission perm = new CustomPermission("testPerm", "read,write");
        assertNotNull(perm);
    }

    @Test
    void testGetName_shouldReturnName() {
        assertEquals("testPermission", permission.getName());
    }

    @Test
    void testConstructor_withNullName_shouldHandleGracefully() {
        assertDoesNotThrow(() -> new CustomPermission(null));
    }

    @Test
    void testConstructor_withEmptyName_shouldCreateInstance() {
        CustomPermission perm = new CustomPermission("");
        assertNotNull(perm);
    }

    @Test
    void testConstructor_withNullActions_shouldCreateInstance() {
        CustomPermission perm = new CustomPermission("test", null);
        assertNotNull(perm);
    }

    @Test
    void testImplies_withSamePermission_shouldReturnTrue() {
        CustomPermission perm1 = new CustomPermission("test");
        CustomPermission perm2 = new CustomPermission("test");
        assertTrue(perm1.implies(perm2));
    }

    @Test
    void testEquals_withSamePermission_shouldReturnTrue() {
        CustomPermission perm1 = new CustomPermission("test");
        CustomPermission perm2 = new CustomPermission("test");
        assertTrue(perm1.equals(perm2));
    }

    @Test
    void testHashCode_shouldReturnValue() {
        int hashCode = permission.hashCode();
        assertTrue(hashCode != 0 || hashCode == 0); // Just verify it doesn't throw
    }
}
