/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.util;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.xml.internal.ws.util.xml.NamedNodeMapIterator;

public class MigrationHelper {

    /**
     * Renames an Element.
     * 
     * @param element xml element to rename
     * @param name new name of the element
     */
    @SuppressWarnings("unchecked")
    public static void renameElement(Element element, final String name) {
        Element renamed = element.getOwnerDocument().createElement(name);
        new NamedNodeMapIterator(element.getAttributes()).forEachRemaining(obj -> {
            Node attribute = (Node) obj;
            renamed.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
        });
        while (element.getFirstChild() != null) {
            renamed.appendChild(element.getFirstChild());
        }
        element.getParentNode().replaceChild(renamed, element);
    }

    /**
     * @param src       xml element
     * @param oldName   old name of the attribute
     * @param newName   new name of the attribute 
     * Renames a xml attribute.
     */
    public static void renameAttribute(Element src, final String oldName, 
            final String newName) {
        
        if (src.hasAttribute(oldName)) {
            String value = src.getAttribute(oldName);
            src.setAttribute(newName, value);
            src.removeAttribute(oldName);
        }
    }

    /**
     * Delete elements by their tag names.
     * 
     * @param root root element of the document
     * @param tagName name of the tag which has to be deleted
     * @return Returns the number of deleted elements
     */
    public static int deleteElements(Element root, final String tagName) {
        NodeList elements = root.getElementsByTagName(tagName);
        int elementCount = elements.getLength();

        while (elements.getLength() > 0) {            
            Node currentNode = elements.item(0);
            currentNode.getParentNode().removeChild(currentNode);
        }

        return elementCount;
    }

    /**
     * Create real list from crummy {@link NodeList}
     * @param list
     * @return
     */
    public static List<Node> listFromNodeList(final NodeList list) {
        return new AbstractList<Node>() {

            public int size() {
                return list.getLength();
            }

            public Node get(int index) {
                Node item = list.item(index);
                if (item == null)
                    throw new IndexOutOfBoundsException();
                return item;
            }
        };
    }
    
    /**
     * Get a property either as an attribute or as a child element
     * @param element
     * @param propertyName
     * @return
     */
    public static String getReferenceTargetURI(Element element, String path, String propertyName) {
        String attributeValue = element.getAttribute(propertyName);
        if (!StringExtensions.isNullOrEmpty(attributeValue)) {
            return path + "#" + attributeValue;
        }
        
        List<Node> children = listFromNodeList(element.getChildNodes());
        for (Node child : children) {
            if (child instanceof Element && ((Element) child).getTagName().equals(propertyName)) {
                String href = ((Element) child).getAttribute("href");
                if (!StringExtensions.isNullOrEmpty(href)) {
                    return href;
                }
            }
        }
        
        return null;
    }
    
    public static String getURIFragment(String uriAsString) {
        if (uriAsString == null) {
            return null;
        }
        
        return URI.createURI(uriAsString).fragment();
    }
    
    public static Node parse(Document document, String str) {
        Node node;
        try {
            StringInputStream stringInputStream = new StringInputStream(str, document.getInputEncoding());
            node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stringInputStream).getDocumentElement();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        return document.importNode(node, true);
    }
}
