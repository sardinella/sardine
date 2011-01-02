/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

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
     * Test method for {@link com.googlecode.sardine.util.SardineUtil#parseDate(java.lang.String)}. 
     * new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US), 
     * new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US), 
     * new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US), 
     * new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US), 
     * new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US), 
     * new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US), 
     * new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
     * 
     * @throws ParseException
     */
    @Test
    public void testParseDate() throws ParseException {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        final Date parsedDate = simpleDateFormat.parse("2010-12-31T13:59:01Z");
        final long expectedTime = parsedDate.getTime();
        final List<String> dates = Arrays.asList(//
                "2010-12-31T13:59:01Z", //
                "Fri, 31 Dec 2010 13:59:01 GMT", //
                "Fri Dec 31 13:59:01 GMT 2010", //
                "Friday, 31-Dec-10 13:59:01 GMT", //
                "Fri December 31 13:59:01 2010");
        for (String date : dates) {
            final long actualTime = SardineUtil.parseDate(date).getTime();        
            assertEquals(expectedTime, actualTime);            
        }

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
        assertEquals(
                //
                STANDARD_DAV_RESOURCE_START + "<D:propfind xmlns:D=\"DAV:\" xmlns:S=\"SAR:\"><D:allprop/></D:propfind>",
                SardineUtil.getDefaultPropfindXML());
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
        final String xml = SardineUtil.getResourcePatchXml(null, Arrays.asList(new String[] {}));
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
        final String xml = SardineUtil.getResourcePatchXml(setProps, Arrays.asList("a", "b"));
        assertThat(xml, containsString("<S:mööp>määp</S:mööp>"));
        assertThat(xml, containsString("<S:foo>bar</S:foo>"));
        assertThat(xml, containsString("<D:remove><D:prop><S:a/><S:b/></D:prop></D:remove>"));
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