package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;

import com.googlecode.sardine.util.SardineException;

/**
 * @author mirko
 *
 */
public final class ExistsResponseHandler extends BasicResponseHandler<Boolean> {
    /**
     * @param url
     */
    public ExistsResponseHandler(String url) {
        super(url);
    }

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