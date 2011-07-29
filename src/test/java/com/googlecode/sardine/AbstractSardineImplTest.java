/**
 * Copyright 2011 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import org.apache.commons.io.IOUtils;
//import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.httpclient.SardineHttpClientImpl;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.util.ResponseToDavResource;
import com.googlecode.sardine.util.SardineUtil;

/**
 * @author mfriedenhagen
 */
public abstract class AbstractSardineImplTest {

    protected static final String GMX_BASE_URL = "https://mediacenter.gmx.net/";

    protected static final String SVN_BASE_URL = "https://svn.jenkins-ci.org/tags/jswidgets-1.5/";

    protected static final String SVN_BASE_URL_STATIC = "https://svn.java.net/svn/hudson~svn/tags/jswidgets-1.5/";

    protected static final String SVN_POM_BASE_URL = SVN_BASE_URL + "pom.xml";

    protected static final String SVN_POM_BASE_URL_STATIC = SVN_BASE_URL_STATIC + "pom.xml";

    protected static final String WEBDE_BASE_URL = "https://webdav.smartdrive.web.de/";

    protected final Sardine sardine;

    public AbstractSardineImplTest() {
        sardine = createSardine();
    }

    protected abstract Sardine createSardine();

    /**
     * Test method for
     * {@link com.googlecode.sardine.httpclient.SardineHttpClientImpl#SardineImpl(com.googlecode.sardine.Factory)}.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testSvnContentIntegrative() throws IOException {
        final HashMap<String, DavResource> resourceMap = toMap(sardine.list(SVN_BASE_URL));
        checkMultipleResources(resourceMap);
    }

    /**
     * @param resources
     */
    protected void checkMultipleResources(final HashMap<String, DavResource> resources) {
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
        final HashMap<String, DavResource> resources = toMap(ResponseToDavResource.fromMultiStatus(
                URI.create(SVN_POM_BASE_URL_STATIC), loadFromResources("svn-propfind-pom.xml")));
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

    @Test
    public void testStaticContent() throws JAXBException, IOException {
        final Multistatus multiStatus = loadFromResources("propfind.xml");
        final List<DavResource> fromMultiStatus = ResponseToDavResource.fromMultiStatus(URI.create(WEBDE_BASE_URL),
                multiStatus);
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
        final List<DavResource> fromMultiStatus = ResponseToDavResource.fromMultiStatus(URI.create(GMX_BASE_URL),
                multiStatus);
        assertEquals(multiStatusSize, fromMultiStatus.size());
        final HashMap<String, DavResource> resources = toMap(fromMultiStatus);
        assertEquals(multiStatusSize, resources.size());
        final DavResource strangeDirectoryName = resources.get("Ich%20&%20Du,%20M%c3%bcllers%20Kuh");
        assertEquals("Ich & Du, Müllers Kuh", strangeDirectoryName.getNameDecoded());
        assertTrue(strangeDirectoryName.isDirectory());
        final DavResource image = resources.get("imm002_0A-9.jpg");
        assertEquals("image/jpeg", image.getContentType());
    }

    /**
     * @param resourcename
     * @return
     * @throws IOException
     * @throws JAXBException
     * @throws IOException
     */
    protected Multistatus loadFromResources(final String resourcename) throws IOException, JAXBException, IOException {
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
    protected HashMap<String, DavResource> toMap(final List<DavResource> resources) {
        final HashMap<String, DavResource> map = new HashMap<String, DavResource>();
        for (final DavResource davResource : resources) {
            map.put(davResource.getName(), davResource);
        }
        return map;
    }

}
