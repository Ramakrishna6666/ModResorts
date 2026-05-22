package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
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
        MBeanInfo mbeanInfo = appInfo.getMBeanInfo();
        
        assertNotNull(mbeanInfo);
        assertEquals("com.acme.modres.mbean.AppInfo", mbeanInfo.getClassName());
        assertEquals("Configurable App Info", mbeanInfo.getDescription());
    }

    @Test
    void testInvoke_increaseMaxLimit() throws MBeanException, ReflectionException {
        Object result = appInfo.invoke("increaseMaxLimit", new Object[]{}, new String[]{});
        
        assertNotNull(result);
        assertEquals("Max limit increased", result);
    }

    @Test
    void testInvoke_resetMaxLimit() throws MBeanException, ReflectionException {
        Object result = appInfo.invoke("resetMaxLimit", new Object[]{}, new String[]{});
        
        assertNotNull(result);
        assertEquals("Max limit reset", result);
    }

    @Test
    void testInvoke_unsupportedOperation_throwsException() {
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke("unsupportedOperation", new Object[]{}, new String[]{});
        });
    }

    @Test
    void testInvoke_withNullActionName_throwsException() {
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke(null, new Object[]{}, new String[]{});
        });
    }

    @Test
    void testInvoke_withEmptyActionName_throwsException() {
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke("", new Object[]{}, new String[]{});
        });
    }

    @Test
    void testGetAttribute_returnsNull() throws AttributeNotFoundException, MBeanException, ReflectionException {
        Object result = appInfo.getAttribute("anyAttribute");
        
        assertNull(result);
    }

    @Test
    void testSetAttribute_doesNotThrow() {
        Attribute attribute = new Attribute("testAttr", "testValue");
        
        assertDoesNotThrow(() -> {
            appInfo.setAttribute(attribute);
        });
    }

    @Test
    void testGetAttributes_returnsNull() {
        AttributeList result = appInfo.getAttributes(new String[]{"attr1", "attr2"});
        
        assertNull(result);
    }

    @Test
    void testSetAttributes_returnsNull() {
        AttributeList attributes = new AttributeList();
        attributes.add(new Attribute("attr1", "value1"));
        
        AttributeList result = appInfo.setAttributes(attributes);
        
        assertNull(result);
    }

    @Test
    void testInvoke_withNullParams() throws MBeanException, ReflectionException {
        Object result = appInfo.invoke("increaseMaxLimit", null, null);
        
        assertNotNull(result);
    }

    @Test
    void testGetMBeanInfo_hasOperations() {
        MBeanInfo mbeanInfo = appInfo.getMBeanInfo();
        
        assertNotNull(mbeanInfo.getOperations());
    }
}
