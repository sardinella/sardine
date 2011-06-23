package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.protocol.HTTP;

/**
 * Simple class for making proppatch a bit easier to deal with.
 */
class HttpPropPatch extends HttpEntityEnclosingRequestBase {
    public HttpPropPatch(String url) {
        super();
        this.setURI(URI.create(url));
        this.setHeader("Content-Type", "text/xml" + HTTP.CHARSET_PARAM + "UTF-8");
    }

    @Override
    public String getMethod() {
        return "PROPPATCH";
    }
}
