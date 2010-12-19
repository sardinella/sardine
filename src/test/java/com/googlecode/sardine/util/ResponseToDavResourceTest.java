/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.googlecode.sardine.model.Collection;
import com.googlecode.sardine.model.Creationdate;
import com.googlecode.sardine.model.Getcontentlength;
import com.googlecode.sardine.model.Getcontenttype;
import com.googlecode.sardine.model.Getlastmodified;
import com.googlecode.sardine.model.Prop;
import com.googlecode.sardine.model.Propstat;
import com.googlecode.sardine.model.Resourcetype;
import com.googlecode.sardine.model.Response;

/**
 * @author mirko
 * 
 */
public class ResponseToDavResourceTest {

    private final Response response = Mockito.mock(Response.class);

    private final Propstat propstat = Mockito.mock(Propstat.class);

    private final Prop prop = new Prop();

    private final Resourcetype resourceType = new Resourcetype();

    @Before
    public void connectMocks() {
        Mockito.when(propstat.getProp()).thenReturn(prop);
        Mockito.when(response.getPropstat()).thenReturn(Arrays.asList(propstat));
        prop.setResourcetype(resourceType);
    }

    /**
     * Test method for
     * {@link com.googlecode.sardine.util.ResponseToDavResource#ResponseToDavResource(com.googlecode.sardine.model.Response, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testResponseToDavResource() {
        newSut(null, null);
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.ResponseToDavResource#retrieveModifiedDate(java.lang.String)}.
     */
    @Test
    public void testRetrieveModifiedDate() {
        final ResponseToDavResource sut = newSut(null, null);
        final Getlastmodified getLastModified = new Getlastmodified();

        // getLastModified is set and has content
        final String expected = "1971-01-01'T'00:00:00.000'Z'";
        getLastModified.getContent().add(expected);
        prop.setGetlastmodified(getLastModified);
        assertEquals(expected, sut.retrieveModifiedDate(ResponseToDavResource.DEFAULT_DATE));

        // // getLastModified is set but has no content
        getLastModified.getContent().clear();
        assertEquals(expected, sut.retrieveModifiedDate(expected));

        // // getLastModified is not set.
        prop.setGetlastmodified(null);
        assertEquals(expected, sut.retrieveModifiedDate(expected));
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.ResponseToDavResource#retrieveCreationDate()}.
     */
    @Test
    public void testRetrieveCreation() {
        final ResponseToDavResource sut = newSut(null, null);
        final Creationdate creationdate = new Creationdate();

        // creationdate is set and has content
        final String expected = "1971-01-01'T'00:00:00.000'Z'";
        creationdate.getContent().add(expected);
        prop.setCreationdate(creationdate);
        assertEquals(expected, sut.retrieveCreationDate());

        // // creationdate is set but has no content
        creationdate.getContent().clear();
        assertEquals(ResponseToDavResource.DEFAULT_DATE, sut.retrieveCreationDate());

        // // creationdate is not set.
        prop.setCreationdate(null);
        assertEquals(ResponseToDavResource.DEFAULT_DATE, sut.retrieveCreationDate());
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.ResponseToDavResource#retrieveContentType()}.
     */
    @Test
    public void testRetrieveContentType() {
        final ResponseToDavResource sut = newSut(null, null);

        // if content-type is null return default.
        assertSame(ResponseToDavResource.DEFAULT_CONTENT_TYPE, sut.retrieveContentType());

        // if content-type has no content return default
        final Getcontenttype getContentType = new Getcontenttype();
        prop.setGetcontenttype(getContentType);
        assertSame(ResponseToDavResource.DEFAULT_CONTENT_TYPE, sut.retrieveContentType());

        // return correct type
        final String expected = "text/html";
        getContentType.getContent().add(expected);
        assertSame(expected, sut.retrieveContentType());

        // always return directory if this is a collection
        resourceType.setCollection(new Collection());
        ResponseToDavResource sut2 = new ResponseToDavResource(response, null, null);
        resourceType.setCollection(new Collection());
        assertSame(ResponseToDavResource.HTTPD_UNIX_DIRECTORY_CONTENT_TYPE, sut2.retrieveContentType());
    }

    /**
     * Test method for {@link com.googlecode.sardine.util.ResponseToDavResource#retrieveContentLength()}.
     */
    @Test
    public void testRetrieveContentLength() {
        final ResponseToDavResource sut = newSut(null, null);

        // no content-length
        assertSame(ResponseToDavResource.DEFAULT_CONTENT_LENGTH, sut.retrieveContentLength());

        // if content-type has no content return default
        final Getcontentlength getContentLength = new Getcontentlength();
        prop.setGetcontentlength(getContentLength);
        assertSame(ResponseToDavResource.DEFAULT_CONTENT_LENGTH, sut.retrieveContentLength());

        final String expected = "1024";
        getContentLength.getContent().add(expected);
        assertSame(expected, sut.retrieveContentLength());
    }

    @Test
    public void testRemoveTrailingSlashFromDirectoryName() {

        final ResponseToDavResource sut = newSut(null, null);
        assertEquals("NoDirectoryWithSlash/", sut.removeTrailingSlashFromDirectoryName("NoDirectoryWithSlash/"));
        assertEquals("NoDirectoryWithoutSlash", sut.removeTrailingSlashFromDirectoryName("NoDirectoryWithoutSlash"));

        resourceType.setCollection(new Collection());
        ResponseToDavResource sut2 = new ResponseToDavResource(response, null, null);
        assertEquals("ADirectory", sut2.removeTrailingSlashFromDirectoryName("ADirectory/"));

    }

    /**
     * Test method for {@link com.googlecode.sardine.util.ResponseToDavResource#toDavResource()}.
     */
    @Test
    @Ignore(value = "Not implemented")
    public void testToDavResource() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @param baseUrl
     * @param hostPart
     * @return
     */
    ResponseToDavResource newSut(final String baseUrl, final String hostPart) {
        return new ResponseToDavResource(response, baseUrl, hostPart);
    }
}
