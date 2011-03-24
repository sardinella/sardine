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
 * Wrapper for the input stream, will consume the rest of the response on {@link ConsumingInputStream#close()}. This class
 * will decorate an {@link IOException} thrown while reading so you know which url was invoked.
 *
 * @author mirko
 */
class ConsumingInputStream extends InputStream {

    private final InputStream inputStream;

    private final String url;

    private final HttpResponse response;

    /**
     * @param url
     *            for exception information
     * @param response
     *            to get the {@link InputStream} from.
     * @throws IllegalStateException
     *             see {@link HttpEntity#getContent()}.
     * @throws SardineException
     *             when {@link HttpResponse#getEntity()} is null.
     * @throws IOException
     *             see {@link HttpEntity#getContent()}.
     */
    public ConsumingInputStream(final String url, final HttpResponse response) throws IllegalStateException,
            SardineException, IOException {
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

    /** {@inheritDoc}
     *
     * This method will consume the content of the {@link HttpEntity}.
     */
    @Override
    public void close() throws IOException {
        response.getEntity().consumeContent();
        inputStream.close();
        super.close();
    }
}
