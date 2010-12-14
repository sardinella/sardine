/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.ObjectFactory;
import com.googlecode.sardine.model.Response;

/**
 * @author mirko
 * 
 */
public class DavResourceTest {

    private final static String BASE_URL = "https://webdav.smartdrive.web.de";
    
    private final JAXBContext context;

    private final Unmarshaller unmarshaller;

    private Multistatus mulitStatus;

    private final DavResource resource;

    private final Date creation = new Date(0);

    private final Date modified = new Date(creation.getTime() + 3600);

    /**
     * @throws JAXBException
     * @throws IOException 
     */
    public DavResourceTest() throws JAXBException, IOException {
        context = JAXBContext.newInstance(ObjectFactory.class);
        unmarshaller = context.createUnmarshaller();
        final InputStream stream = DavResourceTest.class.getResourceAsStream("propfind.xml");
        try {
            mulitStatus = (Multistatus) unmarshaller.unmarshal(stream);
        } finally {
            stream.close();
        }
        resource = new DavResource(BASE_URL, "Meine%20Anlagen", creation, modified, "httpd/unix-directory", 0L, true, null);
    }

    /**
     * Test method for
     * {@link com.googlecode.sardine.DavResource#DavResource(java.lang.String, java.lang.String, java.util.Date, java.util.Date, java.lang.String, java.lang.Long, boolean, java.util.Map)}
     * .
     */
    @Test
    public void testDavResource() {
        final DavResource resource = new DavResource(BASE_URL, "Meine%20Anlagen", creation, modified, "httpd/unix-directory", 0L, true, null);
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
        assertTrue(resource.isCurrentDirectory());
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
        assertEquals("DavResource [baseUrl=https://webdav.smartdrive.web.de, contentLength=0, contentType=httpd/unix-directory, creation=Thu Jan 01 01:00:00 CET 1970, modified=Thu Jan 01 01:00:03 CET 1970, name=Meine%20Anlagen, nameDecoded=Meine Anlagen, getAbsoluteUrl()=https://webdav.smartdrive.web.de/Meine%20Anlagen/, isDirectory()=true]", String.valueOf(resource));
    }

}
