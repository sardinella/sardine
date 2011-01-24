package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Simple class for making proppatch a bit easier to deal with.
 */
class HttpPropPatch extends HttpEntityEnclosingRequestBase {
    public HttpPropPatch(String url) {
        super();
        this.setURI(URI.create(url));
        this.setHeader("Content-Type", "text/xml");
    }

    @Override
    public String getMethod() {
        return "PROPPATCH";
    }
}
