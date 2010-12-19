package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

/**
 * {@link org.apache.http.client.ResponseHandler} which just executes the request and checks the answer is
 * in the valid range of {@link BasicResponseHandler#checkGoodResponse(HttpResponse, String)}.
 * 
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
