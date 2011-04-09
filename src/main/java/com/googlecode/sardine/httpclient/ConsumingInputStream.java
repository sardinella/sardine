/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

/**
 * Wrapper for the input stream, will consume the rest of the response on {@link ConsumingInputStream#close()}. This
 * class will decorate an {@link IOException} thrown while reading so you know which url was invoked.
 *
 * @author mirko
 */
class ConsumingInputStream extends InputStream {

    private final InputStream delegate;

    private final HttpResponse response;

    /**
     * @param url
     *            for exception information
     * @param response
     *            to get the {@link InputStream} from.
     * @throws IllegalStateException
     *             see {@link HttpEntity#getContent()}.
     * @throws IOException
     *             when when {@link HttpResponse#getEntity()} is null or {@link HttpEntity#getContent()}.
     */
    public ConsumingInputStream(final String url, final HttpResponse response) throws IllegalStateException,
            IOException {
        this.response = response;
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new IOException("Has no entity" + url);
        } else {
            this.delegate = entity.getContent();
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public void close() throws IOException {
        EntityUtils.consume(response.getEntity());
    }
}
