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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.StringEntity;
import org.junit.Ignore;
import org.junit.Test;

import com.googlecode.sardine.model.Prop;

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
     * 
     * @throws IOException
     */
    @Test
    public void testGetResourcePatchEntity() throws IOException {
        final StringEntity patchEntityWithTwoElements = SardineUtil.getResourcePatchEntity(null, Arrays.asList("A", "ö"));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:S=\"SAR:\"><D:remove><D:prop><S:A/><S:ö/></D:prop></D:remove></D:propertyupdate>",
                IOUtils.toString(patchEntityWithTwoElements.getContent()).replaceAll("\n", ""));
        final StringEntity patchEntityWithEmptyRemovalList = SardineUtil.getResourcePatchEntity(null, Arrays.asList(new String[]{}));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:S=\"SAR:\"><D:remove><D:prop></D:prop></D:remove></D:propertyupdate>",
                IOUtils.toString(patchEntityWithEmptyRemovalList.getContent()).replaceAll("\n", ""));
    }

    @Test
    public void testGetResourcePatchEntity2() throws IOException {
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("foo", "bar");
        map.put("mööp", "määp");
        final StringEntity patchEntityWithTwoElements = SardineUtil.getResourcePatchEntity2(map, Arrays.asList("a", "b"));
        System.out.println(IOUtils.toString(patchEntityWithTwoElements.getContent()));
        System.out.println(IOUtils.toString(SardineUtil.GET_RESOURCES.getContent()));
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
        System.out.println(SardineUtil.GET_RESOURCES.getContentType());
    }
}
