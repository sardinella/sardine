/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;

import com.googlecode.sardine.util.SardineException;
import com.googlecode.sardine.util.SardineUtil;

/**
 * @author mirko
 * 
 */
public abstract class BasicResponseHandler<T> implements ResponseHandler<T> {

    /**
     * 
     */
    private final String url;

    /**
     * 
     */
    public BasicResponseHandler(final String url) {
        this.url = url;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param response
     * @param msg
     * @throws SardineException
     */
    protected void checkGoodResponse(HttpResponse response, String msg) throws SardineException {
        final StatusLine statusLine = response.getStatusLine();
        if (!SardineUtil.isGoodResponse(statusLine.getStatusCode())) {
            throw new SardineException(msg, getUrl(), statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
    }

}
