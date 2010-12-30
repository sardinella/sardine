/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.googlecode.sardine.model.Prop;
import com.googlecode.sardine.model.Propertyupdate;
import com.googlecode.sardine.model.Remove;
import com.googlecode.sardine.model.Set;

/**
 * 
 * Factory class for &lt;propertyupdate&gt; elements for the PROPPATCH method. Example:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 * &lt;D:propertyupdate xmlns:D="DAV:" xmlns:S="SAR:">
 *   &lt;D:set>
 *       &lt;D:prop>
 *           &lt;S:foo>bar&lt;/S:foo>
 *           &lt;S:mööp>määp&lt;/S:mööp>
 *       &lt;/D:prop>
 *   &lt;/D:set>
 *   &lt;D:remove>
 *       &lt;D:prop>
 *           &lt;S:a/>
 *           &lt;S:b/>
 *       &lt;/D:prop>
 *   &lt;/D:remove>
 * &lt;/D:propertyupdate>
 * </pre>
 * 
 * @author mirko
 */
class PropertyupdateFactory {

    /**
     * 
     */
    public static final String DEFAULT_NAMESPACE_PREFIX = "S";

    /**
     * 
     */
    public static final String DEFAULT_NAMESPACE_URI = "SAR:";

    private final Propertyupdate propertyupdate = new Propertyupdate();

    private final Map<QName, String> propsToBeSet;

    private final List<QName> propsToBeRemoved;

    private final Document document;

    /**
     * Creates a {@link Propertyupdate} element containing all properties to set from setProps and all properties to
     * remove from removeProps. Note this method will use a {@link PropertyupdateFactory#DEFAULT_NAMESPACE_URI} as
     * namespace and {@link PropertyupdateFactory#DEFAULT_NAMESPACE_PREFIX} as prefix.
     * 
     * @param propsToBeSet
     * @param propsToBeRemoved
     * @return
     */
    public static Propertyupdate newPropertyupdate(final Map<String, String> propsToBeSet, final List<String> propsToBeRemoved) {
        return new PropertyupdateFactory(//
                propsToBeSet == null ? null : toQNameMap(propsToBeSet), //
                propsToBeRemoved == null ? null : toQNameList(propsToBeRemoved)//
        ).get();
    }

    private PropertyupdateFactory(Map<QName, String> setProps, List<QName> removeProps) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder;
        try {
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Message:", e);
        }
        document = documentBuilder.newDocument();
        this.propsToBeSet = setProps;
        this.propsToBeRemoved = removeProps;
    }

    private static Map<QName, String> toQNameMap(Map<String, String> setProps) {
        final HashMap<QName, String> result = new HashMap<QName, String>(setProps.size());
        for (final Entry<String, String> entry : setProps.entrySet()) {
            result.put(newQNameWithDefaultNamespace(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private static List<QName> toQNameList(List<String> removeProps) {
        final ArrayList<QName> result = new ArrayList<QName>(removeProps.size());
        for (final String entry : removeProps) {
            result.add(newQNameWithDefaultNamespace(entry));
        }
        return result;
    }

    private static QName newQNameWithDefaultNamespace(final String key) {
        return new QName(DEFAULT_NAMESPACE_URI, key, DEFAULT_NAMESPACE_PREFIX);
    }

    private Propertyupdate get() {
        if (propsToBeSet != null) {
            addPropsToBeSet();
        }
        if (propsToBeRemoved != null) {
            addPropsToBeRemoved();
        }
        return propertyupdate;
    }

    private void addPropsToBeSet() {
        final Set set = new Set();
        propertyupdate.getRemoveOrSet().add(set);
        final Prop prop = new Prop();
        set.setProp(prop);
        final List<Element> any = prop.getAny();
        for (Entry<QName, String> entry : propsToBeSet.entrySet()) {
            final Element element = newElementFromQName(entry.getKey());
            element.setTextContent(entry.getValue());
            any.add(element);
        }
    }

    private void addPropsToBeRemoved() {
        final Remove remove = new Remove();
        propertyupdate.getRemoveOrSet().add(remove);
        final Prop prop = new Prop();
        remove.setProp(prop);
        final List<Element> any = prop.getAny();
        for (QName entry : propsToBeRemoved) {
            final Element element = newElementFromQName(entry);
            any.add(element);
        }
    }

    private Element newElementFromQName(final QName key) {
        return document.createElementNS(key.getNamespaceURI(), key.getPrefix() + ":" + key.getLocalPart());
    }

}
