/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Before;
import org.mockito.Mockito;

/**
 * @author mirko
 * 
 */
public class ResponseHandlerTestBase {

    protected final HttpResponse response = Mockito.mock(HttpResponse.class);

    protected final StatusLine statusLine = Mockito.mock(StatusLine.class);

    @Before
    public void connectMocks() {
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
    }

}
