/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.util;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
    public void testEncodeAndDecode() {
        final String expected = "äöü ß";
        assertEquals(expected, SardineUtil.decode(SardineUtil.encode(expected)));
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
    public void testGetDefaultPropfindXML() {
        final String defaultPropfindXML = SardineUtil.getDefaultPropfindXML();
        checkXmlDeclaration(defaultPropfindXML);
        assertThat(defaultPropfindXML, containsString("allprop/>"));
    }

    /**
     * @param xml
     */
    void checkXmlDeclaration(final String xml) {
        assertTrue(xml + " must start with " + STANDARD_DAV_RESOURCE_START, xml.startsWith(STANDARD_DAV_RESOURCE_START));
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
        checkXmlDeclaration(xml);
        assertThat(xml, containsString("remove>"));
        assertThat(xml, containsString("S:ö"));
        assertThat(xml, containsString("S:A"));
    }

    @Test
    public void testGetResourcePatchXmlWithEmptyRemovalList() throws IOException {
        final String xml = SardineUtil.getResourcePatchXml(null, Arrays.asList(new String[] {}));
        checkXmlDeclaration(xml);
        assertThat(xml, containsString("remove>"));
        assertThat(xml, containsString("prop/>"));
    }

    @Test
    public void testGetResourcePatchXmlCombined() throws IOException {
        HashMap<String, String> setProps = new HashMap<String, String>();
        setProps.put("foo", "bar");
        setProps.put("mööp", "määp");
        final String xml = SardineUtil.getResourcePatchXml(setProps, Arrays.asList("a", "b"));
        checkXmlDeclaration(xml);
        assertThat(xml, containsString("määp</S:mööp>"));
        assertThat(xml, containsString("bar</S:foo>"));
        assertThat(
                xml,
                anyOf(containsString("<D:remove><D:prop><S:a/><S:b/></D:prop></D:remove>"),
                      containsString("<remove><prop><S:a xmlns:S=\"SAR:\"/><S:b xmlns:S=\"SAR:\"/></prop></remove>")));

    }

    /**
     * Test method for
     * {@link com.googlecode.sardine.util.SardineUtil#getMultistatus(javax.xml.bind.Unmarshaller, java.io.InputStream, java.lang.String)}
     * .
     *
     * @throws IOException
     */
    @Test
    public void testGetMultistatus() {
        try {
            SardineUtil.getMultistatus(SardineUtil.createUnmarshaller(), new ByteArrayInputStream("noxml".getBytes()),
                    "http://webdav.example.com/");
            fail("Expected a IOException.");
        } catch (IOException e) {
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
        final String xml = SardineUtil.getDefaultPropfindXML();
        checkXmlDeclaration(xml);
        assertThat(xml, containsString("propfind>"));
        assertThat(xml, containsString("allprop/>"));
    }
}
