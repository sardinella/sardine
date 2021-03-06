package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Simple class for making mkcol a bit easier to deal with.
 */
class HttpMkCol extends HttpEntityEnclosingRequestBase {
    public HttpMkCol(String url) {
        super();
        this.setURI(URI.create(url));
    }

    /** {@inheritDoc} */
    @Override
    public String getMethod() {
        return "MKCOL";
    }
}
