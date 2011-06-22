/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;

/**
 * Basic response handler which takes an url for documentation.
 *
 * @author mirko
 *
 * @param <T>
 *            return type of {@link ResponseHandler#handleResponse(HttpResponse)}.
 */
abstract class BasicResponseHandler<T> implements ResponseHandler<T> {

    /** url for documentation and error messages. */
    private final String url;

    /**
     * @param url
     *            for documentation and error messages.
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
     * and throws an {@link IOException} otherwise.
     *
     * @param response
     *            to check
     * @param errorMessage
     *            to add
     * @throws IOException
     *             when the status code is not acceptable.
     */
    void checkGoodResponse(HttpResponse response, String errorMessage) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        final int statusCode = statusLine.getStatusCode();
        if (!((statusCode >= HttpStatus.SC_OK) && (statusCode < HttpStatus.SC_MULTIPLE_CHOICES))) {
            throw new IOException(errorMessage + " for: " + getUrl() + ", " + statusLine.getStatusCode() + " "
                    + statusLine.getReasonPhrase());
        }
    }
}
