/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;

import com.googlecode.sardine.util.SardineException;

/**
 * @author mirko
 * 
 */
public abstract class BasicResponseHandler<T> implements ResponseHandler<T> {

    /**
     * 
     */
    private final String url;

    /**
     * 
     */
    public BasicResponseHandler(final String url) {
        this.url = url;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param response
     * @param msg
     * @throws SardineException
     */
    void checkGoodResponse(HttpResponse response, String msg) throws SardineException {
        final StatusLine statusLine = response.getStatusLine();
        final int statusCode = statusLine.getStatusCode();
        if (!((statusCode >= HttpStatus.SC_OK) && (statusCode < HttpStatus.SC_MULTIPLE_CHOICES))) {
            throw new SardineException(msg, getUrl(), statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
    }

}
