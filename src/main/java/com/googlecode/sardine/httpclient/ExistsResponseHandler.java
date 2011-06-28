package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link org.apache.http.client.ResponseHandler} which checks whether a given resource exists.
 *
 * @author mirko
 */
final class ExistsResponseHandler extends BasicResponseHandler<Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(ExistsResponseHandler.class);

    /**
     * @param url
     */
    public ExistsResponseHandler(String url) {
        super(url);
    }

    /**
     * {@inheritDoc}
     *
     * Returns true when the statusCode is less than {@link HttpStatus#SC_MULTIPLE_CHOICES} and false when we found a
     * {@link HttpStatus#SC_NOT_FOUND}, otherwise throws a {@link IOException}.
     */
    public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        final StatusLine statusLine = response.getStatusLine();
        final int statusCode = statusLine.getStatusCode();
        final String reasonPhrase = statusLine.getReasonPhrase();
        LOG.trace("{}: {} {}", new Object[] { getUrl(), statusCode, reasonPhrase });
        if (statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
            return true;
        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
            return false;
        } else {
            throw new IOException("Could not check for url: " + getUrl() + ", " + statusCode + " " + reasonPhrase);
        }
    }
}
