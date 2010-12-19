/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import org.junit.Test;

import com.googlecode.sardine.util.SardineException;

/**
 * @author mirko
 *
 */
public class HttpCopyTest {

    /**
     * Test method for {@link com.googlecode.sardine.httpclient.HttpCopy#HttpCopy(java.lang.String, java.lang.String)}.
     * @throws SardineException 
     */
    @Test(expected=SardineException.class)
    public void testHttpCopy() throws SardineException {
        new HttpCopy("http://webdav.example.com/foo/", "http://webdav.example.com/bar");
    }

}
