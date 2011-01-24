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
 * Basic response handler which takes an url for documentation.
 * 
 * @author mirko
 * 
 * @param <T>
 *            return type of {@link ResponseHandler#handleResponse(HttpResponse)}.
 */
public abstract class BasicResponseHandler<T> implements ResponseHandler<T> {

    /** url for documentation. */
    private final String url;

    /**
     * @param url
     *            for documentation.
     */
    protected BasicResponseHandler(final String url) {
        this.url = url;
    }

    /**
     * Returns the url.
     * 
     * @return the url
     */
    String getUrl() {
        return url;
    }

    /**
     * Checks the response for a statuscode between {@link HttpStatus#SC_OK} and {@link HttpStatus#SC_MULTIPLE_CHOICES}
     * and throws an {@link SardineException} otherwise.
     * 
     * @param response
     *            to check
     * @param msg
     *            to add
     * @throws SardineException
     *             when the status code is not acceptable.
     */
    void checkGoodResponse(HttpResponse response, String msg) throws SardineException {
        final StatusLine statusLine = response.getStatusLine();
        final int statusCode = statusLine.getStatusCode();
        if (!((statusCode >= HttpStatus.SC_OK) && (statusCode < HttpStatus.SC_MULTIPLE_CHOICES))) {
            throw new SardineException(msg, getUrl(), statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
    }
}
