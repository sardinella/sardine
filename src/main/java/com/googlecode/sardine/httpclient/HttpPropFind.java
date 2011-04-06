package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.protocol.HTTP;

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
        this.setHeader("Content-Type", "text/xml" + HTTP.CHARSET_PARAM + "UTF-8");
    }

    /** {@inheritDoc} */
    @Override
    public final String getMethod() {
        return "PROPFIND";
    }
}
