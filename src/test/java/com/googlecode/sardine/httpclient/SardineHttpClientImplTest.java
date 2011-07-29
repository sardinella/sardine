/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import com.googlecode.sardine.AbstractSardineImplTest;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.util.ResponseToDavResource;

/**
 * @author mirko
 *
 */
public class SardineHttpClientImplTest extends AbstractSardineImplTest {

    /** {@inheritDoc} */
    @Override
    protected Sardine createSardine() {
        // TODO Auto-generated method stub
        try {
            return new SardineHttpClientImpl();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("Message:", e);
        }
    }


    @Test
    public void testSvnContentStatic() throws JAXBException, IOException {
        final HashMap<String, DavResource> resourceMap = toMap(ResponseToDavResource.fromMultiStatus(URI.create(SVN_BASE_URL_STATIC),
                loadFromResources("svn-propfind.xml")));
        checkMultipleResources(resourceMap);
    }

    @Test(expected=ClientProtocolException.class)
    public void wrapResponseHandlerExceptionsClientProtocolException() throws IOException {
        ((SardineHttpClientImpl)sardine).wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new ResponseHandler<Void>() {
            public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                throw new ClientProtocolException();
            }
        });
    }

    @Test(expected=IOException.class)
    public void wrapResponseHandlerExceptionsIOException() throws IOException {
        ((SardineHttpClientImpl)sardine).wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new ResponseHandler<Void>() {
            public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                throw new IOException();
            }
        });
    }

    @Test
    public void wrapResponseHandlerExceptionsAuthenticationException() throws IOException {
        SardineHttpClientImpl sut = new SardineHttpClientImpl() {
            /** {@inheritDoc} */
            @Override
            void setAuthenticationOnMethod(HttpRequestBase base) throws AuthenticationException {
                throw new AuthenticationException();
            }
        };
        try {
            sut.wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new BasicResponseHandler());
        } catch (IOException e) {
            assertEquals(AuthenticationException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testInjectedHttpClient() throws IOException {
        final DefaultHttpClient httpClient = new DefaultHttpClient(HttpClientUtils.createDefaultHttpParams());
        HttpClientUtils.enableCompression(httpClient);
        final SardineHttpClientImpl sardine = new SardineHttpClientImpl(httpClient);
        checkMultipleResources(toMap(sardine.list(SVN_BASE_URL)));
    }

    @Test(expected=HttpResponseException.class)
    public void testExpectationFailedTwice() throws ClientProtocolException, IOException {
        final String uri = "http://example.com/";
        final HttpPut request = new HttpPut(uri);
        final SardineHttpClientImpl sardine = new SardineHttpClientImpl(new DefaultHttpClient()) {
            /** {@inheritDoc} */
            @Override
            <T> T wrapResponseHandlerExceptions(HttpRequestBase request, ResponseHandler<T> responseHandler)
                    throws IOException {
                throw new HttpResponseException(HttpStatus.SC_EXPECTATION_FAILED, "Expectation failed");
            }
        };
        sardine.put(uri, request, new StringEntity("hallo"), "text/xml", true);
    }

    @Test
    public void testExpectationFailedOnce() throws ClientProtocolException, IOException {
        final String uri = "http://example.com/";
        final HttpPut request = new HttpPut(uri);
        final SardineHttpClientImpl sardine = new SardineHttpClientImpl(new DefaultHttpClient()) {
            int i = 0;
            /** {@inheritDoc} */
            @Override
            <T> T wrapResponseHandlerExceptions(HttpRequestBase request, ResponseHandler<T> responseHandler)
                    throws IOException {
                if (i == 0) {
                    i++;
                    throw new HttpResponseException(HttpStatus.SC_EXPECTATION_FAILED, "Expectation failed");
                } else {
                    return null;
                }
            }
        };
        sardine.put(uri, request, new StringEntity("hallo"), "text/xml", true);
    }

    @Test
    public void testIsStatusExpectationFailedAndEntityRepeatable() throws UnsupportedEncodingException {
        final SardineHttpClientImpl sardine = new SardineHttpClientImpl(new DefaultHttpClient());
        final HttpResponseException exceptionExpectationFailed = new HttpResponseException(HttpStatus.SC_EXPECTATION_FAILED, "Expectation failed");
        final HttpResponseException otherException = new HttpResponseException(HttpStatus.SC_CONFLICT, "Conflict");
        final StringEntity repeatableEntity = new StringEntity("hallo");
        final InputStreamEntity nonRepeatableEntity = new InputStreamEntity(new ByteArrayInputStream("hallo".getBytes()), -1);
        assertTrue(repeatableEntity.isRepeatable());
        assertFalse(nonRepeatableEntity.isRepeatable());
        assertTrue(sardine.isStatusExpectationFailedAndEntityRepeatable(exceptionExpectationFailed, repeatableEntity));
        assertFalse(sardine.isStatusExpectationFailedAndEntityRepeatable(otherException, repeatableEntity));
        assertFalse(sardine.isStatusExpectationFailedAndEntityRepeatable(exceptionExpectationFailed, nonRepeatableEntity));
        assertFalse(sardine.isStatusExpectationFailedAndEntityRepeatable(otherException, nonRepeatableEntity));
    }

}
