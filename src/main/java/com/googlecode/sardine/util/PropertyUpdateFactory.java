/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package com.googlecode.sardine.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
 * @author mirko
 * 
 */
class PropertyUpdateFactory {
    
    private final Propertyupdate propertyupdate = new Propertyupdate();

    private final Map<String, String> setProps;

    private final List<String> removeProps;

    private final Document document;

    public static Propertyupdate newPropertyupdate(Map<String, String> setProps, List<String> removeProps) {
        return new PropertyUpdateFactory(setProps, removeProps).get();
    }
    
    private PropertyUpdateFactory(Map<String, String> setProps, List<String> removeProps) {
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

    private Propertyupdate get() {
        if (setProps != null) {
            createSetProps();
        }
        if (removeProps != null) {
            createRemoveProps();
        }
        return propertyupdate;
    }

    /**
     * @param propertyupdate
     * @param setProps
     */
    private void createSetProps() {
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
