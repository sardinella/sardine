/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.junit.Test;

import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.ObjectFactory;
import com.googlecode.sardine.model.Propfind;
import com.googlecode.sardine.util.SardineException;
import com.googlecode.sardine.util.SardineUtil;
import com.googlecode.sardine.util.SardineUtil.HttpPropFind;

/**
 * @author mirko
 * 
 */
public class SardineImplTest {

    /**
     * 
     */
    private static final String SVN_BASE_URL = "https://svn.java.net/svn/hudson~svn/tags/jswidgets-1.5/";
    private final static String WEBDE_BASE_URL = "https://webdav.smartdrive.web.de/";
    
    private static void setHttpClientLogging() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "INFO");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
    }

    /**
     * Test method for {@link com.googlecode.sardine.SardineImpl#SardineImpl(com.googlecode.sardine.Factory)}.
     * @throws SardineException 
     */
    @Test
    public void testSardineImplFactory() throws SardineException {
        // setHttpClientLogging();
        final Sardine sardine = SardineFactory.begin();
        final HashMap<String, DavResource> resources = toMap(sardine
                .getResources(SVN_BASE_URL));
        checkResources(resources);
    }

    @Test
    public void testStaticSvnContent() throws JAXBException, IOException {
        final Multistatus multiStatus = loadFromResources("svn-propfind.xml");
        final SardineImpl sardine = new SardineImpl(Factory.instance());
        final List<DavResource> fromMultiStatus = sardine.fromMultiStatus(URI.create(SVN_BASE_URL), multiStatus);
        final HashMap<String, DavResource> resources = toMap(fromMultiStatus);
        checkResources(resources);        
    }
    
    /**
     * @param resources
     */
    void checkResources(final HashMap<String, DavResource> resources) {
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
    public void testStaticContent() throws JAXBException, IOException {
        final Multistatus multiStatus = loadFromResources("propfind.xml");
        final SardineImpl sardine = new SardineImpl(Factory.instance());
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

    /**
     * @param resourcename
     * @return
     * @throws SardineException
     * @throws JAXBException
     * @throws IOException
     */
    Multistatus loadFromResources(final String resourcename) throws SardineException, JAXBException, IOException {
        final Unmarshaller unmarshaller = Factory.instance().getUnmarshaller();        
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
        for (DavResource davResource : resources) {
            map.put(davResource.getName(), davResource);
        }
        return map;
    }

}
