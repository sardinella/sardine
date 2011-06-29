/**
 * Copyright 2010 Mirko Friedenhagen
 */

package com.googlecode.sardine.httpclient;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
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

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.DavResourceTest;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.util.SardineUtil;

/**
 * @author mirko
 *
 */
public class SardineHttpClientImplTest {

    private static final String GMX_BASE_URL = "https://mediacenter.gmx.net/";

    private static final String SVN_BASE_URL = "https://svn.jenkins-ci.org/tags/jswidgets-1.5/";
    private static final String SVN_BASE_URL_STATIC = "https://svn.java.net/svn/hudson~svn/tags/jswidgets-1.5/";
    private static final String SVN_POM_BASE_URL = SVN_BASE_URL + "pom.xml";
    private static final String SVN_POM_BASE_URL_STATIC = SVN_BASE_URL_STATIC + "pom.xml";

    private final static String WEBDE_BASE_URL = "https://webdav.smartdrive.web.de/";

    private final SardineHttpClientImpl sardine;

    /**
     * @throws IOException
     *
     */
    public SardineHttpClientImplTest() throws IOException {
        sardine = new SardineHttpClientImpl();
    }

    /**
     * Test method for {@link com.googlecode.sardine.httpclient.SardineHttpClientImpl#SardineImpl(com.googlecode.sardine.Factory)}.
     *
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testSvnContentIntegrative() throws ClientProtocolException, IOException {
        // setHttpClientLogging();
        final HashMap<String, DavResource> resourceMap = toMap(sardine.list(SVN_BASE_URL));
        checkMultipleResources(resourceMap);
    }

    @Test
    public void testSvnContentStatic() throws JAXBException, IOException {
        final HashMap<String, DavResource> resourceMap = toMap(sardine.fromMultiStatus(URI.create(SVN_BASE_URL_STATIC),
                loadFromResources("svn-propfind.xml")));
        checkMultipleResources(resourceMap);
    }

    /**
     * @param resources
     */
    void checkMultipleResources(final HashMap<String, DavResource> resources) {
        assertEquals(4, resources.size());
        final DavResource srcDirectory = resources.get("src");
        assertThat("srcDirectory.isDirectory()", srcDirectory.isDirectory(), is(true));
        assertThat("srcDirectory.isCurrentDirectory()", srcDirectory.isCurrentDirectory(), is(false));
        final DavResource rootDirectory = resources.get("");
        assertThat("rootDirectory.isDirectory()", rootDirectory.isDirectory(), is(true));
        assertThat("rootDirectory.isCurrentDirectory()", rootDirectory.isCurrentDirectory(), is(true));
        assertThat(rootDirectory.getBaseUrl(), is(rootDirectory.getAbsoluteUrl()));
        assertThat(rootDirectory.getCustomProps().get("ignore"), containsString("target\n"));
        DavResource pom = resources.get("pom.xml");
        assertThat(pom.getContentType(), is("text/xml; charset=\"utf-8\""));
    }

    @Test
    public void testPomContentIntegrative() throws IOException {
        final HashMap<String, DavResource> resources = toMap(sardine.list(SVN_POM_BASE_URL));
        checkPom(resources);
    }

    @Test
    public void testGetPomContentIntegrative() throws IOException {
        final InputStream stream = sardine.get(SVN_POM_BASE_URL);
        try {
            assertEquals(7863, IOUtils.toString(stream).length());
        } finally {
            stream.close();
        }
    }
    /**
     * @throws IOException
     */
    @Test
    public void testExists() throws IOException {
        assertTrue(sardine.exists("http://www.google.com/"));
    }

    /**
     * @throws IOException
     */
    @Test
    public void testDoesNotExist() throws IOException {
        assertFalse(sardine.exists("http://www.google.com/idnotexist"));
    }

    @Test
    public void testPomContentStatic() throws JAXBException, IOException {
        final HashMap<String, DavResource> resources = toMap(sardine.fromMultiStatus(URI.create(SVN_POM_BASE_URL_STATIC),
                loadFromResources("svn-propfind-pom.xml")));
        checkPom(resources, SVN_BASE_URL_STATIC, SVN_POM_BASE_URL_STATIC);
    }

    /**
     * @param pomResources
     */
    void checkPom(final HashMap<String, DavResource> pomResources) {
        final String svnBaseUrl = SVN_BASE_URL;
        final String svnPomBaseUrl = SVN_POM_BASE_URL;
        checkPom(pomResources, svnBaseUrl, svnPomBaseUrl);
    }

    /**
     * @param pomResources
     * @param svnBaseUrl
     * @param svnPomBaseUrl
     */
    void checkPom(final HashMap<String, DavResource> pomResources, final String svnBaseUrl, final String svnPomBaseUrl) {
        assertEquals(1, pomResources.size());
        final DavResource pom = pomResources.get("pom.xml");
        assertFalse(pom.isDirectory());
        assertEquals(svnBaseUrl, pom.getBaseUrl());
        assertEquals(svnPomBaseUrl, pom.getAbsoluteUrl());
    }

    @Test(expected=ClientProtocolException.class)
    public void wrapResponseHandlerExceptionsClientProtocolException() throws IOException {
        sardine.wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new ResponseHandler<Void>() {
            public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                throw new ClientProtocolException();
            }
        });
    }

    @Test(expected=IOException.class)
    public void wrapResponseHandlerExceptionsIOException() throws IOException {
        sardine.wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new ResponseHandler<Void>() {
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
    public void testStaticContent() throws JAXBException, IOException {
        final Multistatus multiStatus = loadFromResources("propfind.xml");
        final List<DavResource> fromMultiStatus = sardine.fromMultiStatus(URI.create(WEBDE_BASE_URL), multiStatus);
        final HashMap<String, DavResource> resources = toMap(fromMultiStatus);
        assertThat(resources.size(), is(16));
        final DavResource meineBilder = resources.get("Meine%20Bilder");
        assertTrue("meineBilder.isDirectory()", meineBilder.isDirectory());
        assertEquals("meineBilder.getNameDecoded()", "Meine Bilder", meineBilder.getNameDecoded());
        final DavResource umlautDirectory = resources.get("Namens-A%cc%88nderung");
        assertTrue("umlautDirectory.isDirectory()", umlautDirectory.isDirectory());
        assertEquals("umlautDirectory.getNameDecoded()", "Namens-Änderung", umlautDirectory.getNameDecoded());
        assertEquals("Sun Dec 07 14:17:14 CET 2008", umlautDirectory.getCreation().toString());
        assertEquals("Sun, 07 Dec 2008 13:17:14 GMT", umlautDirectory.getCustomProps().get("Win32CreationTime"));
    }

    @Test
    public void testAnotherStaticContent() throws IOException, JAXBException, IOException {
        final Multistatus multiStatus = loadFromResources("propfind2.xml");
        final int multiStatusSize = multiStatus.getResponse().size();
        final List<DavResource> fromMultiStatus = sardine.fromMultiStatus(URI.create(GMX_BASE_URL), multiStatus);
        assertEquals(multiStatusSize, fromMultiStatus.size());
        final HashMap<String, DavResource> resources = toMap(fromMultiStatus);
        assertEquals(multiStatusSize, resources.size());
        final DavResource strangeDirectoryName = resources.get("Ich%20&%20Du,%20M%c3%bcllers%20Kuh");
        assertEquals("Ich & Du, Müllers Kuh", strangeDirectoryName.getNameDecoded());
        assertTrue(strangeDirectoryName.isDirectory());
        final DavResource image = resources.get("imm002_0A-9.jpg");
        assertEquals("image/jpeg", image.getContentType());
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
    /**
     * @param resourcename
     * @return
     * @throws IOException
     * @throws JAXBException
     * @throws IOException
     */
    Multistatus loadFromResources(final String resourcename) throws IOException, JAXBException, IOException {
        final Unmarshaller unmarshaller = SardineUtil.createUnmarshaller();
        final InputStream stream = DavResourceTest.class.getResourceAsStream(resourcename);
        try {
            return (Multistatus) unmarshaller.unmarshal(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * @param resources
     * @return
     */
    HashMap<String, DavResource> toMap(final List<DavResource> resources) {
        final HashMap<String, DavResource> map = new HashMap<String, DavResource>();
        for (final DavResource davResource : resources) {
            map.put(davResource.getName(), davResource);
        }
        return map;
    }

}
