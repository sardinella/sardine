package com.googlecode.sardine.httpclient;

import java.io.IOException;

import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;

import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.util.SardineUtil;

/**
 * {@link org.apache.http.client.ResponseHandler} which returns the {@link Multistatus} response of a
 * {@link HttpPropFind} request.
 * 
 * @author mirko
 */
final class MultiStatusResponseHandler extends BasicResponseHandler<Multistatus> {

    /**
     *
     */
    private final Unmarshaller unmarshaller;

    /**
     * @param url
     *            used for error reports.
     * @param unmarshaller
     *            to be injected.
     */
    public MultiStatusResponseHandler(final String url, final Unmarshaller unmarshaller) {
        super(url);
        this.unmarshaller = unmarshaller;
    }

    public Multistatus handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        checkGoodResponse(response, "Failed to get resources. Is the url valid?");
        // Process the response from the server.
        final HttpEntity entity = response.getEntity();
        final StatusLine statusLine = response.getStatusLine();
        if (entity == null) {
            throw new IOException("No entity found in response for " + getUrl() + ", " + statusLine.getStatusCode()
                    + " " + statusLine.getReasonPhrase());
        }
        return SardineUtil.getMultistatus(unmarshaller, entity.getContent(), getUrl());
    }
}
