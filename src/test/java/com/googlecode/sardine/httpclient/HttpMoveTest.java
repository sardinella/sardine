/**
 * Copyright 2011 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import org.junit.Test;

import com.googlecode.sardine.util.SardineException;


/**
 * @author mirko
 *
 */
public class HttpMoveTest {

    /**
     * Test method for {@link HttpMove#HttpMove(String, String)}.
     * @throws SardineException
     */
    @Test(expected=SardineException.class)
    public void testHttpMove() throws SardineException
    {
        new HttpMove("http://webdav.example.com/foo/", "http://webdav.example.com/bar");
    }

}
