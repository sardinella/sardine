/**
 * Copyright 2011 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

/**
 * @author mirko
 */
public class VoidResponseHandlerTest extends ResponseHandlerTestBase {

    /**
     * Test method for
     * {@link com.googlecode.sardine.httpclient.VoidResponseHandler#handleResponse(org.apache.http.HttpResponse)}.
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testHandleResponseValid() throws ClientProtocolException, IOException {
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        new VoidResponseHandler("http://www.example.com", "Oops").handleResponse(response);
    }

    /**
     * Test method for
     * {@link com.googlecode.sardine.httpclient.VoidResponseHandler#handleResponse(org.apache.http.HttpResponse)}.
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test(expected=IOException.class)
    public void testHandleResponseInValid() throws ClientProtocolException, IOException {
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_GATEWAY);
        when(statusLine.getReasonPhrase()).thenReturn("Bad Gateway");
        new VoidResponseHandler("http://www.example.com", "Oops").handleResponse(response);
    }
}
