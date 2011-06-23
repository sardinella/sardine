/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.junit.Test;

/**
 * @author mirko
 *
 */
public class HttpCopyTest {

    /**
     * Test method for {@link com.googlecode.sardine.httpclient.HttpCopy#HttpCopy(java.lang.String, java.lang.String, boolean)}.
     * @throws IOException
     */
    @Test(expected=IllegalArgumentException.class)
    public void testHttpCopyOfDirectoryThrowsExceptionOnCopyToFile() throws IOException {
        new HttpCopy("http://webdav.example.com/foo/", "http://webdav.example.com/bar", true);
    }

}
