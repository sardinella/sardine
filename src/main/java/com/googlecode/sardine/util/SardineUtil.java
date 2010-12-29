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
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.entity.StringEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.googlecode.sardine.model.Allprop;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.ObjectFactory;
import com.googlecode.sardine.model.Prop;
import com.googlecode.sardine.model.Propertyupdate;
import com.googlecode.sardine.model.Propfind;
import com.googlecode.sardine.model.Remove;
import com.googlecode.sardine.model.Set;

/**
 * Basic utility code. I borrowed some code from the webdavlib for parsing dates.
 * 
 * @author jonstevens
 */
public class SardineUtil {

    /** */
    final static JAXBContext CONTEXT;

    /** cached version of getResources() webdav xml GET request */
    final static StringEntity GET_RESOURCES;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(ObjectFactory.class);

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        final Propfind propfind = new Propfind();
        propfind.setAllprop(new Allprop());
        GET_RESOURCES = newXmlStringEntityFromJaxbElement(propfind);
    }

    /**
     * @param jaxbElement
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    static StringEntity newXmlStringEntityFromJaxbElement(final Object jaxbElement) {
        final String xml = newXmlStringFromJaxbElement(jaxbElement);
        return newXmlStringEntityFromString(xml);
    }

    /**
     * @param jaxbElement
     * @return
     */
    static String newXmlStringFromJaxbElement(final Object jaxbElement) {
        final StringWriter writer = new StringWriter();
        try {
            final Marshaller marshaller = CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(jaxbElement, writer);
        } catch (JAXBException e) {
            throw new RuntimeException("Error converting " + jaxbElement, e);
        }
        return writer.toString();
    }

    /**
     * @param jaxbElementXml
     * @return
     */
    static StringEntity newXmlStringEntityFromString(final String jaxbElementXml) {
        final StringEntity stringEntity;
        try {
            stringEntity = new StringEntity(jaxbElementXml, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not get encoding UTF-8?" + jaxbElementXml, e);
        }
        stringEntity.setContentType("text/xml; charset=UTF-8");
        return stringEntity;
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
    public static StringEntity getResourcesEntity() {
        return GET_RESOURCES;
    }

    /**
     * Build PROPPATCH entity.
     */
    public static StringEntity getResourcePatchEntity(Map<String, String> setProps, List<String> removeProps) {
        final StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
        sb.append("<D:propertyupdate xmlns:D=\"DAV:\" xmlns:S=\"SAR:\">\n");

        if (setProps != null) {
            sb.append("<D:set>\n");
            sb.append("<D:prop>\n");
            for (Map.Entry<String, String> prop : setProps.entrySet()) {
                sb.append("<S:");
                sb.append(prop.getKey()).append(">");
                sb.append(prop.getValue()).append("</S:");
                sb.append(prop.getKey()).append(">\n");
            }
            sb.append("</D:prop>\n");
            sb.append("</D:set>\n");
        }

        if (removeProps != null) {
            sb.append("<D:remove>\n");
            sb.append("<D:prop>\n");
            for (String removeProp : removeProps) {
                sb.append("<S:");
                sb.append(removeProp).append("/>");
            }
            sb.append("</D:prop>\n");
            sb.append("</D:remove>\n");
        }

        sb.append("</D:propertyupdate>\n");
        try {
            return new StringEntity(sb.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not find encoding, JVM broken?", e);
        }
    }
    static class GetResourcePatchEntity {
        private final Propertyupdate propertyupdate = new Propertyupdate();
        private final Map<String, String> setProps;
        private final List<String> removeProps;
        private final Document document;

        GetResourcePatchEntity(Map<String, String> setProps, List<String> removeProps) {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder;
            try {
                documentBuilder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException("Message:", e);
            }
            document = documentBuilder.newDocument();
            this.setProps = setProps;
            this.removeProps = removeProps;
        }
        
        public StringEntity getResourcePatchEntity() {
            if (setProps != null) {
            createSetProps();
            }
            if (removeProps != null) {
                createRemoveProps();
            }
            return newXmlStringEntityFromJaxbElement(propertyupdate);
        }
        
        /**
         * @param propertyupdate
         * @param setProps
         */
        void createSetProps() {
            final Set set = new Set();
            propertyupdate.getRemoveOrSet().add(set);
            final Prop prop = new Prop();
            set.setProp(prop);
            final List<Element> any = prop.getAny();
            for (Entry<String, String> entry : setProps.entrySet()) {
                final Element element = document.createElementNS("SAR:", "S:" + entry.getKey());                
                element.setTextContent(entry.getValue());
                any.add(element);
            }
        }
        
        /**
         * 
         */
        private void createRemoveProps() {
            final Remove remove = new Remove();
            propertyupdate.getRemoveOrSet().add(remove);
            final Prop prop = new Prop();
            remove.setProp(prop);
            final List<Element> any = prop.getAny();
            for (String entry : removeProps) {
                final Element element = document.createElementNS("SAR:", "S:" + entry);                
                any.add(element);
            }
        }

        

    }
    public static StringEntity getResourcePatchEntity2(Map<String, String> setProps, List<String> removeProps) {
        return new GetResourcePatchEntity(setProps, removeProps).getResourcePatchEntity();
    }

    /**
     * Helper method for getting the Multistatus response processor.
     */
    public static Multistatus getMultistatus(Unmarshaller unmarshaller, InputStream stream, String url)
            throws SardineException {
        try {
            return (Multistatus) unmarshaller.unmarshal(stream);
        } catch (JAXBException ex) {
            throw new SardineException("Problem unmarshalling the data", url, ex);
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
    public static Map<String, String> extractCustomProps(List<Element> elements) {
        final Map<String, String> customPropsMap = new HashMap<String, String>(elements.size());

        for (final Element element : elements) {
            final String[] keys = element.getTagName().split(":", 2);
            final String key = (keys.length > 1) ? keys[1] : keys[0];

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

}
