package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Simple class for making propfind a bit easier to deal with.
 */
public class HttpPropFind extends HttpEntityEnclosingRequestBase {
    public HttpPropFind(String url) {
        super();
        this.setDepth(1);
        this.setURI(URI.create(url));
        this.setHeader("Content-Type", "text/xml");
    }

    @Override
    public final String getMethod() {
        return "PROPFIND";
    }

    public final void setDepth(int val) {
        this.setHeader("Depth", String.valueOf(val));
    }
}
