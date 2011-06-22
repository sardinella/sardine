package com.googlecode.sardine.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

/**
 * {@link org.apache.http.client.ResponseHandler} which just executes the request and checks the answer is in the valid
 * range of {@link BasicResponseHandler#checkGoodResponse(HttpResponse, String)}.
 * 
 * @author mirko
 */
final class VoidResponseHandler extends BasicResponseHandler<Void> {

    private final String msg;

    /**
     * @param url
     *            used for error reports.
     * @param errorMessage
     *            used for error reports when the response is not good.
     */
    public VoidResponseHandler(String url, final String errorMessage) {
        super(url);
        this.msg = errorMessage;

    }

    /** {@inheritDoc} */
    public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        checkGoodResponse(response, msg);
        return null;
    }
}
