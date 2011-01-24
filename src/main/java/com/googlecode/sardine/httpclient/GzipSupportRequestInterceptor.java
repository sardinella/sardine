package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * This {@link HttpRequestInterceptor} adds an Accept-Encoding of gzip. 
 */
final class GzipSupportRequestInterceptor implements HttpRequestInterceptor {
    
    /** {@inheritDoc} */
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        if (!request.containsHeader("Accept-Encoding")) {
            request.addHeader("Accept-Encoding", "gzip");
        }
    }
}
