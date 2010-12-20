/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import javax.xml.bind.UnmarshalException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author mirko
 * 
 */
public class SardineUtilTest {

    /**
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#encode(java.lang.String)}.
     */
    @Test
    @Ignore
    public void testEncode() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#decode(java.lang.String)}.
     */
    @Test
    @Ignore
    public void testDecode() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#parseDate(java.lang.String)}.
     */
    @Test
    @Ignore
    public void testParseDate() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#isGoodResponse(int)}.
     */
    @Test
    public void testIsGoodResponse() {
        assertFalse(SardineUtil.isGoodResponse(199));
        assertTrue(SardineUtil.isGoodResponse(200));
        assertTrue(SardineUtil.isGoodResponse(299));
        assertFalse(SardineUtil.isGoodResponse(300));
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#getResourcesEntity()}.
     */
    @Test
    @Ignore
    public void testGetResourcesEntity() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for
     * {@link com.googlecode.sardine.util.SardineUtil#getResourcePatchEntity(java.util.Map, java.util.List)}.
     */
    @Test
    @Ignore
    public void testGetResourcePatchEntity() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for
     * {@link com.googlecode.sardine.util.SardineUtil#getMultistatus(javax.xml.bind.Unmarshaller, java.io.InputStream, java.lang.String)}
     * .
     * 
     * @throws SardineException
     */
    @Test
    public void testGetMultistatus() {
        try {
            SardineUtil.getMultistatus(SardineUtil.createUnmarshaller(), new ByteArrayInputStream("noxml".getBytes()),
                    "http://webdav.example.com/");
            fail("Expected a SardineException.");
        } catch (SardineException e) {
            assertEquals(UnmarshalException.class, e.getCause().getClass());
        }
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#extractCustomProps(java.util.List)}.
     */
    @Test
    @Ignore
    public void testExtractCustomProps() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#createUnmarshaller()}.
     */
    @Test
    @Ignore
    public void testCreateUnmarshaller() {
        fail("Not yet implemented"); // TODO
    }

}
