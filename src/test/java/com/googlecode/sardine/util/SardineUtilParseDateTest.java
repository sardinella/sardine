/**
 * Copyright 2011 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test class for {@link com.googlecode.sardine.util.SardineUtil#parseDate(java.lang.String)}.
 * new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
 * new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
 * new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US),
 * new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
 * new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
 * new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
 * new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
 *
 * @author mirko
 *
 */
@RunWith(Parameterized.class)
public class SardineUtilParseDateTest {

    private final String dateToParse;

    private long expectedTime;

    @Parameters
    public static List<Object[]> data() {
        final List<Object[]> dates = Arrays.asList(new Object[][]{//
                {"2010-12-31T13:59:01Z",}, //
                {"Fri, 31 Dec 2010 13:59:01 GMT",}, //
                {"Fri Dec 31 13:59:01 GMT 2010"}, //
                {"Friday, 31-Dec-10 13:59:01 GMT"}, //
                {"Fri December 31 13:59:01 2010"}});
        return dates;
    }

    /**
     * @throws ParseException 
     * 
     */
    public SardineUtilParseDateTest(final String dateToParse) throws ParseException {
        this.dateToParse = dateToParse;
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        final Date parsedDate = simpleDateFormat.parse("2010-12-31T13:59:01Z");
        expectedTime = parsedDate.getTime();

    }

    /**
     *
     * @throws ParseException
     */
    @Test
    public void testParseDate() throws ParseException {
        final long actualTime = SardineUtil.parseDate(dateToParse).getTime();
        assertEquals(expectedTime, actualTime);
    }

}
