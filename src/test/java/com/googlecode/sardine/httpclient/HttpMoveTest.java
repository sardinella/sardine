/**
 * Copyright 2011 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.junit.Test;


/**
 * @author mirko
 *
 */
public class HttpMoveTest {

    /**
     * Test method for {@link HttpMove#HttpMove(String, String)}.
     * @throws SardineException
     */
    @Test(expected=IllegalArgumentException.class)
    public void testHttpMoveOfDirectoryThrowsExceptionOnMoveToFile() throws IOException
    {
        new HttpMove("http://webdav.example.com/foo/", "http://webdav.example.com/bar");
    }

}
