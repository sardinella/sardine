/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;


/**
 */
public class UrlUriTest {
    
    @Test
    public void validUri() throws URISyntaxException, MalformedURLException {
        final URI uri = new URI("http://webdav.smartdrive.web.de:80/abc/../bde");
        assertEquals("webdav.smartdrive.web.de", uri.getHost());
        assertEquals("http", uri.getScheme());
        assertEquals(80, uri.getPort());
        final URL url = uri.normalize().toURL();
        assertEquals("http://webdav.smartdrive.web.de:80/bde", url.toExternalForm());
        assertEquals("http://webdav.smartdrive.web.de:80/foo", uri.resolve(new URI("foo")).toURL().toExternalForm());
        assertEquals("http://webdav.smartdrive.web.de:80/foo/bde", uri.relativize(new URI("http://webdav.smartdrive.web.de:80/foo/bde")).toURL().toExternalForm());        
    }

    @Test(expected=MalformedURLException.class)
    public void inValidUri() throws MalformedURLException {
        new URL("Öäö");
    }
}
