/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author mirko
 *
 */
public class BasicResponseHandlerTest extends ResponseHandlerTestBase {

    /**
     * Test method for {@link com.googlecode.sardine.httpclient.BasicResponseHandler#checkGoodResponse(org.apache.http.HttpResponse, java.lang.String)}.
     * @throws IOException
     */
    @Test(expected=IOException.class)
    public void testCheckGoodResponseUpperLimit() throws IOException {
        Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MULTIPLE_CHOICES);
        Mockito.when(statusLine.getReasonPhrase()).thenReturn("Multiple Choices");
        new BasicResponseHandler<String>("http://webdav.example.com") {
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                return null;
            }
        }.checkGoodResponse(response, "Oops");
    }
    /**
     * Test method for {@link com.googlecode.sardine.httpclient.BasicResponseHandler#checkGoodResponse(org.apache.http.HttpResponse, java.lang.String)}.
     * @throws IOException
     */
    @Test(expected=IOException.class)
    public void testCheckGoodResponseLowerLimit() throws IOException {
        Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CONTINUE);
        Mockito.when(statusLine.getReasonPhrase()).thenReturn("Continue");
        new BasicResponseHandler<String>("http://webdav.example.com") {
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                return null;
            }
        }.checkGoodResponse(response, "Oops");
    }

}
