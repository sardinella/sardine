/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.googlecode.sardine.util.SardineException;

/**
 * @author mirko
 *
 */
public class SardineImplTest {

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
//        setHttpClientLogging();
        final Sardine sardine = SardineFactory.begin();
        final List<DavResource> resources = sardine.getResources("https://svn.java.net/svn/hudson~svn/tags/jswidgets-1.5/");        
        assertEquals(4, resources.size());
        final HashMap<String,DavResource> map = new HashMap<String, DavResource>();
        for (DavResource davResource : resources) {
            map.put(davResource.getName(), davResource);
        }
        final DavResource srcDirectory = map.get("src");
        assertThat("srcDirectory.isDirectory()", srcDirectory.isDirectory(), is(true));
        assertThat("srcDirectory.isCurrentDirectory()", srcDirectory.isCurrentDirectory(), is(false));
        final DavResource rootDirectory = map.get("");
        assertThat("rootDirectory.isDirectory()", rootDirectory.isDirectory(), is(true));
        assertThat("rootDirectory.isCurrentDirectory()", rootDirectory.isCurrentDirectory(), is(true));
        assertThat(rootDirectory.getBaseUrl(), is(rootDirectory.getAbsoluteUrl()));
        assertThat(rootDirectory.getCustomProps().get("ignore"), containsString("target\n"));
        DavResource pom = map.get("pom.xml");
        assertThat(pom.getContentType(), is("text/xml; charset=\"utf-8\""));        
    }
}
