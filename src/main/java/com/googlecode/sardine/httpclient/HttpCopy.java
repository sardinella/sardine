package com.googlecode.sardine.httpclient;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;


/**
 * Simple class for making copy a bit easier to deal with. Assumes Overwrite = T.
 */
class HttpCopy extends HttpEntityEnclosingRequestBase {
    public HttpCopy(String sourceUrl, String destinationUrl, boolean overwrite) throws IOException {
        super();
        HttpClientUtils.checkConsistentSlashes(sourceUrl, destinationUrl);
        this.setHeader(HttpHeaders.DESTINATION, destinationUrl);
        this.setHeader(HttpHeaders.OVERWRITE, overwrite ? "T" : "F");
        this.setURI(URI.create(sourceUrl));

    }

    /** {@inheritDoc} */
    @Override
    public String getMethod() {
        return "COPY";
    }
}
