package com.googlecode.sardine.httpclient;

import java.io.IOException;

import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.util.SardineUtil;

/**
 * @author mirko
 * 
 */
public final class MultiStatusResponseHandler extends BasicResponseHandler<Multistatus> {

    /**
     * 
     */
    private final Unmarshaller unmarshaller;

    /**
     * @param url
     * @param unmarshaller
     */
    public MultiStatusResponseHandler(final String url, final Unmarshaller unmarshaller) {
        super(url);
        this.unmarshaller = unmarshaller;
    }

    public Multistatus handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        checkGoodResponse(response, "Failed to get resources. Is the url valid?");
        // Process the response from the server.
        return SardineUtil.getMultistatus(unmarshaller, response, getUrl());
    }
}
