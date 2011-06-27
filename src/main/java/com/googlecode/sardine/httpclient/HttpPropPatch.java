package com.googlecode.sardine.httpclient;

import java.net.URI;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.protocol.HTTP;

/**
 * Simple class for making proppatch a bit easier to deal with.
 */
class HttpPropPatch extends HttpEntityEnclosingRequestBase {
    public HttpPropPatch(String url) {
        super();
        this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml" + HTTP.CHARSET_PARAM + "UTF-8");
        this.setURI(URI.create(url));
    }

    /** {@inheritDoc} */
    @Override
    public String getMethod() {
        return "PROPPATCH";
    }
}
