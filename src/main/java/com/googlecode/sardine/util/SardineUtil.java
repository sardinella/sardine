package com.googlecode.sardine.util;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;

import com.googlecode.sardine.model.Allprop;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.ObjectFactory;
import com.googlecode.sardine.model.Propfind;

/**
 * Basic utility code. I borrowed some code from the webdavlib for parsing dates.
 * 
 * @author jonstevens
 */
public class SardineUtil {

    /** */
    public final static JAXBContext CONTEXT;

    /** cached version of getResources() webdav xml GET request */
    private final static String DEFAULT_PROPFIND_XML;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(ObjectFactory.class);

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        final Propfind propfind = new Propfind();
        propfind.setAllprop(new Allprop());
        DEFAULT_PROPFIND_XML = newXmlStringFromJaxbElement(propfind);
    }

    /**
     * Date formats used for Date parsing.
     */
    static final SimpleDateFormat FORMATS[] = { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
            new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };

    /**
     * GMT timezone.
     */
    final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

    static {
        for (final SimpleDateFormat format : FORMATS) {
            format.setTimeZone(GMT_ZONE);
        }
    }

    /**
     * Hides the irritating declared exception.
     */
    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Hides the irritating declared exception.
     * 
     * @return null if there is an IllegalArgumentException
     * @throws RuntimeException
     *             if there is an UnsupportedEncodingException
     */
    public static String decode(String value) {
        try {
            return URLDecoder.decode(value, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Loops over all the possible date formats and tries to find the right one.
     * 
     * @param dateValue
     *            to parse
     * @return a valid {@link Date} or null if none of the formats matched.
     */
    public static Date parseDate(String dateValue) {
        if (dateValue == null)
            return null;

        Date date = null;
        for (final SimpleDateFormat format : FORMATS) {
            try {
                date = ((SimpleDateFormat) format.clone()).parse(dateValue);
                break;
            } catch (ParseException e) {
                // We loop through this until we found a valid one.
                continue;
            }
        }

        return date;
    }

    /**
     * Is the status code 2xx
     */
    public static boolean isGoodResponse(int statusCode) {
        return ((statusCode >= 200) && (statusCode <= 299));
    }

    /**
     * Stupid wrapper cause it needs to be in a try/catch
     */
    public static String getDefaultPropfindXML() {
        return DEFAULT_PROPFIND_XML;
    }

    public static String getResourcePatchXml(Map<String, String> setProps, List<String> removeProps) {
        return newXmlStringFromJaxbElement(PropertyupdateFactory.newPropertyupdate(setProps, removeProps));
    }

    /**
     * Helper method for getting the Multistatus response processor.
     */
    public static Multistatus getMultistatus(Unmarshaller unmarshaller, InputStream stream, String url)
            throws SardineException {
        try {
            return (Multistatus) unmarshaller.unmarshal(stream);
        } catch (JAXBException ex) {
            throw new SardineException("Problem unmarshalling the data from: ", url, ex);
        }
    }

    /**
     * Creates a simple Map from the given custom properties of a response. This implementation does not take into
     * account name spaces.
     * 
     * @param elements
     *            custom properties.
     * @return a map from the custom properties.
     */
    public static Map<String, String> extractCustomProps(final List<Element> elements) {
        final Map<String, String> customPropsMap = new HashMap<String, String>(elements.size());

        for (final Element element : elements) {
            final String key = element.getLocalName();
            //System.out.println(element.getTagName() + "@@" + element.getNamespaceURI() + "@@" + element.getLocalName());
            customPropsMap.put(key, element.getTextContent());
        }

        return customPropsMap;
    }

    /**
     * Creates an {@link Unmarshaller} from the {@link SardineUtil#CONTEXT}.
     * 
     * @return a new Unmarshaller.
     * @throws JAXBException
     */
    public static Unmarshaller createUnmarshaller() {
        try {
            return CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create unmarshaller", e);
        }
    }

    /**
     * @param jaxbElement
     * @return
     */
    public static String newXmlStringFromJaxbElement(final Object jaxbElement) {
        final StringWriter writer = new StringWriter();
        try {
            final Marshaller marshaller = CONTEXT.createMarshaller();
//            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(jaxbElement, writer);
        } catch (JAXBException e) {
            throw new RuntimeException("Error converting " + jaxbElement, e);
        }
        return writer.toString();
    }

}
