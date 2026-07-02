package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppInfoTest {

    private AppInfo appInfo;

    @BeforeEach
    void setUp() {
        appInfo = new AppInfo();
    }

    @Test
    void testConstructor() {
        assertNotNull(appInfo);
    }

    @Test
    void testGetMBeanInfo() {
        MBeanInfo info = appInfo.getMBeanInfo();
        assertNotNull(info);
    }

    @Test
    void testGetMBeanInfo_hasDescription() {
        MBeanInfo info = appInfo.getMBeanInfo();
        assertNotNull(info.getDescription());
    }

    @Test
    void testInvoke_increaseMaxLimit() throws MBeanException, ReflectionException {
        Object result = appInfo.invoke("increaseMaxLimit", null, null);
        assertNotNull(result);
        assertEquals("Max limit increased", result);
    }

    @Test
    void testInvoke_resetMaxLimit() throws MBeanException, ReflectionException {
        Object result = appInfo.invoke("resetMaxLimit", null, null);
        assertNotNull(result);
        assertEquals("Max limit reset", result);
    }

    @Test
    void testInvoke_unsupportedOperation() {
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke("unsupportedOperation", null, null);
        });
    }

    @Test
    void testGetAttribute() throws Exception {
        Object result = appInfo.getAttribute("testAttribute");
        assertNull(result);
    }

    @Test
    void testSetAttribute() {
        Attribute attribute = new Attribute("testAttribute", "testValue");
        assertDoesNotThrow(() -> appInfo.setAttribute(attribute));
    }

    @Test
    void testGetAttributes() {
        AttributeList result = appInfo.getAttributes(new String[]{"attr1", "attr2"});
        assertNull(result);
    }

    @Test
    void testSetAttributes() {
        AttributeList attributes = new AttributeList();
        AttributeList result = appInfo.setAttributes(attributes);
        assertNull(result);
    }

    @Test
    void testInvoke_withNullActionName() {
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke(null, null, null);
        });
    }

    @Test
    void testInvoke_withEmptyActionName() {
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke("", null, null);
        });
    }
}
