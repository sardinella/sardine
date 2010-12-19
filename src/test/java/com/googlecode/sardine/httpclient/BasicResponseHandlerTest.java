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

import com.googlecode.sardine.util.SardineException;

/**
 * @author mirko
 *
 */
public class BasicResponseHandlerTest extends ResponseHandlerTestBase {

    /**
     * Test method for {@link com.googlecode.sardine.httpclient.BasicResponseHandler#checkGoodResponse(org.apache.http.HttpResponse, java.lang.String)}.
     * @throws SardineException 
     */
    @Test(expected=SardineException.class)
    public void testCheckGoodResponse() throws SardineException {
        Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MULTIPLE_CHOICES);
        Mockito.when(statusLine.getReasonPhrase()).thenReturn("Multiple Choices");
        new BasicResponseHandler<String>("http://webdav.example.com") {
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                return null;
            }
        }.checkGoodResponse(response, "Oops");
    }

}
