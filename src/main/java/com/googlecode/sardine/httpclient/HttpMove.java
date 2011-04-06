package com.googlecode.sardine.httpclient;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;


/**
 * Simple class for making move a bit easier to deal with.
 */
class HttpMove extends HttpEntityEnclosingRequestBase {
    public HttpMove(String sourceUrl, String destinationUrl) throws IOException {
        super();
        HttpClientUtils.checkConsistentSlashes(sourceUrl, destinationUrl);
        this.setHeader("Destination", destinationUrl);
        this.setHeader("Overwrite", "T");
        this.setURI(URI.create(sourceUrl));
    }

    @Override
    public String getMethod() {
        return "MOVE";
    }
}
