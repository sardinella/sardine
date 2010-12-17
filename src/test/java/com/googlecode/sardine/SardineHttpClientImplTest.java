/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import com.googlecode.sardine.httpclient.HttpClientUtils;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.util.SardineException;
import com.googlecode.sardine.util.SardineUtil;

/**
 * @author mirko
 * 
 */
public class SardineHttpClientImplTest {

    /**
     * 
     */
    private static final String SVN_BASE_URL = "https://svn.java.net/svn/hudson~svn/tags/jswidgets-1.5/";

    /**
     * 
     */
    private static final String SVN_POM_BASE_URL = SVN_BASE_URL + "pom.xml";

    private final static String WEBDE_BASE_URL = "https://webdav.smartdrive.web.de/";

    final SardineHttpClientImpl sardine;

    /**
     * @throws SardineException
     * 
     */
    public SardineHttpClientImplTest() throws SardineException {
        sardine = new SardineHttpClientImpl();
    }

    private static void setHttpClientLogging() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "INFO");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
    }

    /**
     * Test method for {@link com.googlecode.sardine.SardineHttpClientImpl#SardineImpl(com.googlecode.sardine.Factory)}.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testSvnContentIntegrative() throws ClientProtocolException, IOException {
        // setHttpClientLogging();
        final HashMap<String, DavResource> resourceMap = toMap(sardine.getResources(SVN_BASE_URL));
        checkMultipleResources(resourceMap);
    }

    @Test
    public void testSvnContentStatic() throws JAXBException, IOException {
        final HashMap<String, DavResource> resourceMap = toMap(sardine.fromMultiStatus(URI.create(SVN_BASE_URL),
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
    public void testPomContentIntegrative() throws SardineException {
        final HashMap<String, DavResource> resources = toMap(sardine.getResources(SVN_POM_BASE_URL));
        checkPom(resources);
    }

    /**
     * @throws SardineException
     */
    @Test
    public void testExists() throws SardineException {
        assertTrue(sardine.exists("http://www.google.com/"));
    }

    /**
     * @throws SardineException
     */
    @Test
    public void testDoesNotExist() throws SardineException {
        assertFalse(sardine.exists("http://www.google.com/idnotexist"));
    }

    @Test
    public void testPomContentStatic() throws JAXBException, IOException {
        final HashMap<String, DavResource> resources = toMap(sardine.fromMultiStatus(URI.create(SVN_POM_BASE_URL),
                loadFromResources("svn-propfind-pom.xml")));
        checkPom(resources);
    }

    /**
     * @param pomResources
     */
    void checkPom(final HashMap<String, DavResource> pomResources) {
        assertEquals(1, pomResources.size());
        final DavResource pom = pomResources.get("pom.xml");
        assertFalse(pom.isDirectory());
        assertEquals(SVN_BASE_URL, pom.getBaseUrl());
        assertEquals(SVN_POM_BASE_URL, pom.getAbsoluteUrl());
    }

    @Test
    public void wrapResponseHandlerExceptionsClientProtocolException() throws SardineException {
        try {
            sardine.wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new ResponseHandler<Void>() {
                public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    throw new ClientProtocolException();
                }
            });
        } catch (SardineException e) {
            assertEquals(ClientProtocolException.class, e.getCause().getClass());
        }
    }

    @Test
    public void wrapResponseHandlerExceptionsIOException() throws SardineException {
        try {
            sardine.wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new ResponseHandler<Void>() {
                public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    throw new IOException();
                }
            });
        } catch (SardineException e) {
            assertEquals(IOException.class, e.getCause().getClass());
        }
    }

    @Test
    public void wrapResponseHandlerExceptionsSardineException() throws SardineException {
        try {
            sardine.wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new ResponseHandler<Void>() {
                public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    throw new SardineException("MSG", "URL");
                }
            });
        } catch (SardineException e) {
            assertNull("Direct SardineException", e.getCause());
        }
    }

    @Test
    public void wrapResponseHandlerExceptionsAuthenticationException() throws SardineException {
        SardineHttpClientImpl sut = new SardineHttpClientImpl() {
            /** {@inheritDoc} */
            @Override
            void setAuthenticationOnMethod(HttpRequestBase base) throws AuthenticationException {
                throw new AuthenticationException();
            }
        };
        try {
            sut.wrapResponseHandlerExceptions(new HttpGet(SVN_BASE_URL), new BasicResponseHandler());
        } catch (SardineException e) {
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
    public void testInjectedHttpClient() throws SardineException {
        final DefaultHttpClient httpClient = new DefaultHttpClient(HttpClientUtils.createDefaultHttpParams());
        HttpClientUtils.enableCompression(httpClient);
        final SardineHttpClientImpl sardine = new SardineHttpClientImpl(httpClient);
        checkMultipleResources(toMap(sardine.getResources(SVN_BASE_URL)));
    }
    
    /**
     * @param resourcename
     * @return
     * @throws SardineException
     * @throws JAXBException
     * @throws IOException
     */
    Multistatus loadFromResources(final String resourcename) throws SardineException, JAXBException, IOException {
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
