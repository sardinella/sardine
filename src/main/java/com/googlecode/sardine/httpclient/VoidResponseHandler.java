package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

/**
 * @author mirko
 */
public final class VoidResponseHandler extends BasicResponseHandler<Void> {

    private final String msg;

    /**
     * @param url
     */
    public VoidResponseHandler(String url, final String msg) {
        super(url);
        this.msg = msg;
        
    }

    /** {@inheritDoc} */
    public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        checkGoodResponse(response, msg);
        return null;
    }
}
