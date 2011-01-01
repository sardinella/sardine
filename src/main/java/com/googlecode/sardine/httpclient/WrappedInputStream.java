/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import com.googlecode.sardine.util.SardineException;

/**
 * Wrapper for the input stream, will consume the rest of the response on {@link WrappedInputStream#close()}.
 * 
 * @author mirko
 */
public class WrappedInputStream extends InputStream {

    private final InputStream inputStream;

    private final String url;

    private final HttpResponse response;

    /**
     * @throws IOException
     * @throws IllegalStateException
     */
    public WrappedInputStream(final String url, final HttpResponse response) throws IllegalStateException, IOException {
        this.url = url;
        this.response = response;
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new SardineException("Has no entity", url);
        } else {
            this.inputStream = entity.getContent();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int read() throws IOException {
        try {
            return inputStream.read();
        } catch (IOException e) {
            throw new IOException("Error while reading from " + url, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        response.getEntity().consumeContent();
        inputStream.close();
    }
}
