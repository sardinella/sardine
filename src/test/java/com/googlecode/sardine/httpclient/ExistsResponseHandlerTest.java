/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.mockito.Mockito;

import com.googlecode.sardine.util.SardineException;

/**
 * @author mirko
 *
 */
public class ExistsResponseHandlerTest extends ResponseHandlerTestBase {

    /**
     * Test method for {@link com.googlecode.sardine.httpclient.ExistsResponseHandler#handleResponse(org.apache.http.HttpResponse)}.
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    @Test(expected=SardineException.class)
    public void testHandleResponse() throws ClientProtocolException, IOException {
        Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_GATEWAY);
        new ExistsResponseHandler("http://webdav.example.com/foo").handleResponse(response);
    }

}
