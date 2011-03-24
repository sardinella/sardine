/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.httpclient;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.mockito.Mockito;

import com.googlecode.sardine.util.SardineException;

/**
 * @author mirko
 * 
 */
public class ConsumingInputStreamTest {

    final HttpResponse response = Mockito.mock(HttpResponse.class);

    final HttpEntity entity = Mockito.mock(HttpEntity.class);

    /**
     * Test method for {@link com.googlecode.sardine.httpclient.ConsumingInputStream#read()}.
     * 
     * @throws IOException
     * @throws IllegalStateException
     */
    @Test
    public void testReadWithException() throws IllegalStateException, IOException {
        Mockito.when(response.getEntity()).thenReturn(entity);
        InputStream mockInputStream = Mockito.mock(InputStream.class);
        Mockito.when(mockInputStream.read()).thenThrow(new IOException("oops"));
        Mockito.when(entity.getContent()).thenReturn(mockInputStream);
        final ConsumingInputStream stream = new ConsumingInputStream("hello", response);
        try {
            try {
                stream.read();
            } catch (IOException e) {
                assertEquals("java.io.IOException: Error while reading from hello", e.toString());
                assertEquals("java.io.IOException: oops", e.getCause().toString());
            }
        } finally {
            stream.close();
        }
    }

    /**
     * Test method for {@link com.googlecode.sardine.httpclient.ConsumingInputStream#close()}.
     * 
     * @throws IOException
     * @throws IllegalStateException
     */
    @Test
    public void testClose() throws IllegalStateException, IOException {
        Mockito.when(response.getEntity()).thenReturn(entity);
        final String expected = "content";
        Mockito.when(entity.getContent()).thenReturn(new ByteArrayInputStream(expected.getBytes()));
        final ConsumingInputStream stream = new ConsumingInputStream("hello", response);
        try {
            assertEquals(expected, IOUtils.toString(stream));
        } finally {
            stream.close();
        }
    }

    /**
     * Test method for
     * {@link com.googlecode.sardine.httpclient.ConsumingInputStream#WrappedInputStream(java.lang.String, org.apache.http.HttpResponse)}
     * .
     * 
     * @throws IOException
     * @throws IllegalStateException
     */
    @Test(expected = SardineException.class)
    public void testWrappedInputStreamSardineException() throws IllegalStateException, IOException {
        new ConsumingInputStream("hello", response);
    }

}
