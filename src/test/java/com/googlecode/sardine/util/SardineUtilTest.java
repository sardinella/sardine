/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.bind.UnmarshalException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author mirko
 * 
 */
public class SardineUtilTest {

    /**
     * 
     */
    private static final String STANDARD_DAV_RESOURCE_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

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
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#getDefaultPropfindXML()}.
     */
    @Test
    @Ignore
    public void testGetResourcesEntity() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for
     * {@link com.googlecode.sardine.util.SardineUtil#getResourcePatchXml(java.util.Map, java.util.List)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetResourcePatchXmlWithTwoRemovalElements() throws IOException {
        final String xml = SardineUtil.getResourcePatchXml(null, Arrays.asList("A", "ö"));
        assertEquals(
                STANDARD_DAV_RESOURCE_START
                        + "<D:propertyupdate xmlns:D=\"DAV:\" xmlns:S=\"SAR:\"><D:remove><D:prop><S:A/><S:ö/></D:prop></D:remove></D:propertyupdate>",
                xml);
    }
    
    @Test
    public void testGetResourcePatchXmlWithEmptyRemovalList() throws IOException {
        final String xml = SardineUtil.getResourcePatchXml(null,
                Arrays.asList(new String[] {}));
        assertEquals(
                STANDARD_DAV_RESOURCE_START
                        + "<D:propertyupdate xmlns:D=\"DAV:\" xmlns:S=\"SAR:\"><D:remove><D:prop/></D:remove></D:propertyupdate>",
                xml);
    }

    @Test
    public void testGetResourcePatchXmlCombined() throws IOException {
        HashMap<String, String> setProps = new HashMap<String, String>();
        setProps.put("foo", "bar");
        setProps.put("mööp", "määp");
        final String removeProps = SardineUtil.getResourcePatchXml(setProps, Arrays.asList("a", "b"));
        assertEquals(
                STANDARD_DAV_RESOURCE_START
                        + "<D:propertyupdate xmlns:D=\"DAV:\" xmlns:S=\"SAR:\"><D:set><D:prop><S:mööp>määp</S:mööp><S:foo>bar</S:foo></D:prop></D:set><D:remove><D:prop><S:a/><S:b/></D:prop></D:remove></D:propertyupdate>",
                removeProps);
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

    @Test
    public void createPropfindXml() {
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><D:propfind xmlns:D=\"DAV:\" xmlns:S=\"SAR:\"><D:allprop/></D:propfind>",
                SardineUtil.getDefaultPropfindXML());
    }
}
