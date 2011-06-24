/**
 * Copyright 2011 Mirko Friedenhagen 
 */

package com.googlecode.sardine;

/**
 * Marker interface for methods which require a Webdav-Server.
 * @author mirko
 */
public @interface WebdavOnly {
    /**
     * @return the WEBDAV method used.
     */
    public String method();
}
