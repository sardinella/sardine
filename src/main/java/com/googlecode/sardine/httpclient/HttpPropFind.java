package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Simple class for making propfind a bit easier to deal with.
 */
class HttpPropFind extends HttpEntityEnclosingRequestBase {

    public HttpPropFind(String url) {
        this(url, 1);
    }

    public HttpPropFind(String url, int depth) {
        super();
        this.setHeader("Depth", String.valueOf(depth));
        this.setURI(URI.create(url));
        this.setHeader("Content-Type", "text/xml");
    }

    /** {@inheritDoc} */
    @Override
    public final String getMethod() {
        return "PROPFIND";
    }
}
