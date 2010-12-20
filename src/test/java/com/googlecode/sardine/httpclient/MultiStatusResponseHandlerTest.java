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
import com.googlecode.sardine.util.SardineUtil;

/**
 * @author mirko
 *
 */
public class MultiStatusResponseHandlerTest extends ResponseHandlerTestBase {
    
    /**
     * Test method for {@link com.googlecode.sardine.httpclient.MultiStatusResponseHandler#handleResponse(org.apache.http.HttpResponse)}.
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    @Test(expected=SardineException.class)
    public void testHandleResponseWithNoEntity() throws ClientProtocolException, IOException {
        Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_ACCEPTED);
        Mockito.when(statusLine.getReasonPhrase()).thenReturn("Accepted");
        new MultiStatusResponseHandler("http://webdav.example.com/", SardineUtil.createUnmarshaller()).handleResponse(response);
    }
}
