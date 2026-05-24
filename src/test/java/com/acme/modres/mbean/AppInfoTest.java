package com.acme.modres.mbean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import javax.management.*;

import static org.junit.jupiter.api.Assertions.*;

class AppInfoTest {

    private AppInfo appInfo;

    @BeforeEach
    void setUp() {
        appInfo = new AppInfo();
    }

    @Test
    void testConstructor_shouldCreateInstance() {
        assertNotNull(appInfo);
    }

    @Test
    void testGetMBeanInfo_shouldReturnMBeanInfo() {
        MBeanInfo info = appInfo.getMBeanInfo();
        assertNotNull(info);
    }

    @Test
    void testGetMBeanInfo_shouldHaveCorrectClassName() {
        MBeanInfo info = appInfo.getMBeanInfo();
        assertEquals("com.acme.modres.mbean.AppInfo", info.getClassName());
    }

    @Test
    void testInvoke_withIncreaseMaxLimit_shouldReturnMessage() throws Exception {
        Object result = appInfo.invoke("increaseMaxLimit", null, null);
        assertEquals("Max limit increased", result);
    }

    @Test
    void testInvoke_withResetMaxLimit_shouldReturnMessage() throws Exception {
        Object result = appInfo.invoke("resetMaxLimit", null, null);
        assertEquals("Max limit reset", result);
    }

    @Test
    void testInvoke_withUnsupportedOperation_shouldThrowException() {
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke("unsupportedOperation", null, null);
        });
    }

    @Test
    void testGetAttribute_shouldReturnNull() throws Exception {
        Object result = appInfo.getAttribute("anyAttribute");
        assertNull(result);
    }

    @Test
    void testSetAttribute_shouldNotThrowException() {
        Attribute attr = new Attribute("test", "value");
        assertDoesNotThrow(() -> appInfo.setAttribute(attr));
    }

    @Test
    void testGetAttributes_shouldReturnNull() {
        AttributeList result = appInfo.getAttributes(new String[]{"attr1", "attr2"});
        assertNull(result);
    }

    @Test
    void testSetAttributes_shouldReturnNull() {
        AttributeList attrs = new AttributeList();
        AttributeList result = appInfo.setAttributes(attrs);
        assertNull(result);
    }

    @Test
    void testInvoke_withNullActionName_shouldThrowException() {
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke(null, null, null);
        });
    }
}
