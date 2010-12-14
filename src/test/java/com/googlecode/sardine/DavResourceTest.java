/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.junit.Test;

/**
 * @author mirko
 * 
 */
public class DavResourceTest {

    private final static String BASE_URL = "https://webdav.smartdrive.web.de";

    private final DavResource resource;

    private final Date creation = new Date(0);

    private final Date modified = new Date(creation.getTime() + 3600);

    /**
     * @throws JAXBException
     * @throws IOException
     */
    public DavResourceTest() throws JAXBException, IOException {
        resource = new DavResource(BASE_URL, "Meine%20Anlagen", creation, modified, "httpd/unix-directory", 0L, false,
                null);
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getBaseUrl()}.
     */
    @Test
    public void testGetBaseUrl() {
        assertEquals(BASE_URL, resource.getBaseUrl());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals("Meine%20Anlagen", resource.getName());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getNameDecoded()}.
     */
    @Test
    public void testGetNameDecoded() {
        assertEquals("Meine Anlagen", resource.getNameDecoded());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getCreation()}.
     */
    @Test
    public void testGetCreation() {
        assertEquals(creation, resource.getCreation());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getModified()}.
     */
    @Test
    public void testGetModified() {
        assertEquals(modified, resource.getModified());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getContentType()}.
     */
    @Test
    public void testGetContentType() {
        assertEquals("httpd/unix-directory", resource.getContentType());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getContentLength()}.
     */
    @Test
    public void testGetContentLength() {
        assertEquals(Long.valueOf(0L), resource.getContentLength());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getAbsoluteUrl()}.
     */
    @Test
    public void testGetAbsoluteUrl() {
        assertEquals("https://webdav.smartdrive.web.de/Meine%20Anlagen/", resource.getAbsoluteUrl());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#isDirectory()}.
     */
    @Test
    public void testIsDirectory() {
        assertTrue(resource.isDirectory());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#isCurrentDirectory()}.
     */
    @Test
    public void testIsCurrentDirectory() {
        assertFalse(resource.isCurrentDirectory());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#getCustomProps()}.
     */
    @Test
    public void testGetCustomProps() {
        assertNull(resource.getCustomProps());
    }

    /**
     * Test method for {@link com.googlecode.sardine.DavResource#toString()}.
     */
    @Test
    public void testToString() {
        assertEquals(
                "DavResource [baseUrl=https://webdav.smartdrive.web.de, contentLength=0, contentType=httpd/unix-directory, creation=Thu Jan 01 01:00:00 CET 1970, modified=Thu Jan 01 01:00:03 CET 1970, name=Meine%20Anlagen, nameDecoded=Meine Anlagen, getAbsoluteUrl()=https://webdav.smartdrive.web.de/Meine%20Anlagen/, isDirectory()=true]",
                String.valueOf(resource));
    }

}
