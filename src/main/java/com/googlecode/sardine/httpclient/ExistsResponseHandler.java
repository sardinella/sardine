package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import com.googlecode.sardine.util.SardineException;

/**
 * {@link org.apache.http.client.ResponseHandler} which checks wether a given resource exists.
 *
 * @author mirko
 */
final class ExistsResponseHandler extends BasicResponseHandler<Boolean> {
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
     * {@link HttpStatus#SC_NOT_FOUND}, otherwise throws a {@link SardineException}.
     */
    public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        final StatusLine statusLine = response.getStatusLine();
        final int statusCode = statusLine.getStatusCode();
        if (statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
            return true;
        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
            return false;
        } else {
            throw new SardineException("Could not check for url", getUrl(), statusCode, statusLine.getReasonPhrase());
        }
    }
}
