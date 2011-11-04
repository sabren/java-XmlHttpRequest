/*
 * $Id: SimpleDocument.java 268 2008-01-05 08:04:06Z rbair $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jdesktop.dom;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.jdesktop.xpath.XPathUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

/**
 * <p>A DOM {@link org.w3c.dom.Document} that makes it easier to work with DOM
 * documents. This class simply wraps a delegate DOM <code>Document</code> and
 * delegates all calls to the Document. This allows this class to work with
 * any DOM Document.</p>
 * 
 * @author rbair
 */
public class SimpleDocument implements Document {
    private Document dom;
    private XPath xpath;
    //save compiled expressions to hopefully improve performance. These cached
    //expressions are saved in SoftReferences, so if memory gets tight they will
    //be released.
    private Map<String, SoftReference<XPathExpression>> cachedExpressions = 
            new HashMap<String, SoftReference<XPathExpression>>();
    
    /**
     * Create a new, empty, SimpleDocument
     */
    public SimpleDocument() {
        this(new SimpleDocumentBuilder().newPlainDocument());
    }
    
    /** 
     * Creates a new instance of SimpleDocument.
     * 
     * @param Document the DOM document to wrap within this SimpleDocument.
     */
    public SimpleDocument(Document dom) {
        if (dom == null) {
            throw new NullPointerException("DOM Cannot be null");
        }
        this.dom = dom;
    }
    
    /**
     * Creates a new instance of SimpleDocument with the given XML. The given
     * XML must be valid.
     * 
     * @param xml
     */
    public SimpleDocument(String xml) {
        try {
            this.dom = SimpleDocumentBuilder.simpleParse(xml);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //------------------------------------------------------- Helper methods
    
    @Override
    public String toString() {
        return toXML();
    }
    
    /**
     * Exports this DOM as a String
     */
    public String toXML() {
        return XPathUtils.toXML(dom);
    }
    
    /*
     * Exports the given DOM Node as an XML document.
     *
     * @param Node the node to use as the root of the XML document
     * @return an XML String with the given Node as the root element
     */
    public String toXML(Node n) {
        SimpleDocument temp = new SimpleDocument();
        Node nn = n.cloneNode(true);
        temp.adoptNode(nn);
        temp.appendChild(nn);
        return temp.toXML();
    }
    
    /**
     * <p>Returns the child elements of the specified node. This returns only the
     * immediate child Nodes of type ELEMENT_NODE.</p>
     *
     * @param parent the parent node
     * @return a SimpleNodeList containing all of the immediate child elements
     */
    public SimpleNodeList getChildElements(Node node) {
        Node[] nodes = new Node[getChildElementCount(node)];
        int index = 0;
        NodeList list = node.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                nodes[index++] = n;
            }
        }
        return new SimpleNodeList(nodes);
    }
    
    /**
     * Returns the number of child elements for the given node.
     * Only immediate child nodes of type ELEMENT_NODE are counted.
     *
     * @param parent the parent node
     * @return the number of immediate child Elements of the given parent node
     */
    public int getChildElementCount(Node node) {
        int count = 0;
        NodeList list = node.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                count++;
            }
        }
        return count;
    }
    
    //-------------------------------------------------- XPath based methods
    
    /**
     * Compiles the specified expression, and caches the expression in a SoftReference,
     * so that under stress it can be reclaimed by the system.
     *
     * @param expression the expression to compile
     * @returns the compiled expression as an XPathExpression
     */
    private XPathExpression compile(String expression) throws XPathExpressionException {
        SoftReference<XPathExpression> ref = cachedExpressions.get(expression);
        XPathExpression e = ref == null ? null : ref.get();
        if (e == null) {
            cachedExpressions.remove(expression);
            e = XPathUtils.compile(expression);
            cachedExpressions.put(expression, new SoftReference<XPathExpression>(e));
        }
        return e;
    }
    
    /**
     * Returns a {@link SimpleNodeList} containing all the nodes that match the given expression.
     * 
     * @param expression an XPath expression
     * @return SimpleNodeList containing the results from the expression. This will
     *         never be null, but may contain no results.
     * @throws IllegalArgumentException if the expression does not parse
     */
    public SimpleNodeList getElements(String expression) {
        try {
            return XPathUtils.getElements(compile(expression), dom);
        } catch (XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     * Returns a {@link SimpleNodeList} containing all the nodes that match the given expression
     * when executed on the given node (as opposed to the dom as a whole).
     * 
     * @param expression an XPath expression
     * @param node the contextual node
     * @return SimpleNodeList containing the results from the expression. This will
     *         never be null, but may contain no results.
     * @throws IllegalArgumentException if the expression does not parse
     */
    public SimpleNodeList getElements(String expression, Node node) {
        try {
            return XPathUtils.getElements(compile(expression), node);
        } catch (XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     * Returns a Node matching the given expression. If more than one node matches,
     * the return value is undefined.
     * 
     * @param expression an XPath expression
     * @return Node. May be null.
     * @throws IllegalArgumentException if the expression does not parse
     */
    public Node getElement(String expression) {
        try {
            return XPathUtils.getElement(compile(expression), dom);
        } catch (XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     * Returns a Node matching the given expression. If more than one node matches,
     * the return value is undefined.
     * 
     * @param expression an XPath expression
     * @param node the contextual node
     * @return Node. May be null.
     * @throws IllegalArgumentException if the expression does not parse
     */
    public Node getElement(String expression, Node node) {
        try {
            return XPathUtils.getElement(compile(expression), node);
        } catch (XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     * Returns the text content of the Node matching the given expression. 
     * If more than one node matches, the return value is undefined.
     * 
     * @param expression an XPath expression
     * @return text content of the selected Node. May be null.
     * @throws IllegalArgumentException if the expression does not parse
     */
    public String getString(String expression) {
        try {
            return XPathUtils.getString(compile(expression), dom);
        } catch (XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     * Returns the text content of the Node matching the given expression. 
     * If more than one node matches, the return value is undefined.
     * 
     * @param expression an XPath expression
     * @param node the contextual node
     * @return text content of the selected Node. May be null.
     * @throws IllegalArgumentException if the expression does not parse
     */
    public String getString(String expression, Node node) {
        try {
            return XPathUtils.getString(compile(expression), node);
        } catch (XPathExpressionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    //--------------------------------------- Standard DOM methods delegated

    /**
     * @inheritDoc
     */
    public DocumentType getDoctype() {
        return dom.getDoctype();
    }

    /**
     * @inheritDoc
     */
    public DOMImplementation getImplementation() {
        return dom.getImplementation();
    }

    /**
     * @inheritDoc
     */
    public Element getDocumentElement() {
        return dom.getDocumentElement();
    }

    /**
     * @inheritDoc
     */
    public Element createElement(String tagName) throws DOMException {
        return dom.createElement(tagName);
    }

    /**
     * @inheritDoc
     */
    public DocumentFragment createDocumentFragment() {
        return dom.createDocumentFragment();
    }

    /**
     * @inheritDoc
     */
    public Text createTextNode(String data) {
        return dom.createTextNode(data);
    }

    /**
     * @inheritDoc
     */
    public Comment createComment(String data) {
        return dom.createComment(data);
    }

    /**
     * @inheritDoc
     */
    public CDATASection createCDATASection(String data) throws DOMException {
        return dom.createCDATASection(data);
    }

    /**
     * @inheritDoc
     */
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        return dom.createProcessingInstruction(target, data);
    }

    /**
     * @inheritDoc
     */
    public Attr createAttribute(String name) throws DOMException {
        return dom.createAttribute(name);
    }

    /**
     * @inheritDoc
     */
    public EntityReference createEntityReference(String name) throws DOMException {
        return dom.createEntityReference(name);
    }

    /**
     * @inheritDoc
     */
    public SimpleNodeList getElementsByTagName(String tagname) {
        return new SimpleNodeList(dom.getElementsByTagName(tagname));
    }

    /**
     * @inheritDoc
     */
    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        return dom.importNode(importedNode, deep);
    }

    /**
     * @inheritDoc
     */
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return dom.createElementNS(namespaceURI, qualifiedName);
    }

    /**
     * @inheritDoc
     */
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        return dom.createAttributeNS(namespaceURI, qualifiedName);
    }

    /**
     * @inheritDoc
     */
    public SimpleNodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return new SimpleNodeList(dom.getElementsByTagNameNS(namespaceURI, localName));
    }

    /**
     * @inheritDoc
     */
    public Element getElementById(String elementId) {
        return dom.getElementById(elementId);
    }

    /**
     * @inheritDoc
     */
    public String getInputEncoding() {
        return dom.getInputEncoding();
    }

    /**
     * @inheritDoc
     */
    public String getXmlEncoding() {
        return dom.getXmlEncoding();
    }

    /**
     * @inheritDoc
     */
    public boolean getXmlStandalone() {
        return dom.getXmlStandalone();
    }

    /**
     * @inheritDoc
     */
    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        dom.setXmlStandalone(xmlStandalone);
    }

    /**
     * @inheritDoc
     */
    public String getXmlVersion() {
        return dom.getXmlVersion();
    }

    /**
     * @inheritDoc
     */
    public void setXmlVersion(String xmlVersion) throws DOMException {
        dom.setXmlVersion(xmlVersion);
    }

    /**
     * @inheritDoc
     */
    public boolean getStrictErrorChecking() {
        return dom.getStrictErrorChecking();
    }

    /**
     * @inheritDoc
     */
    public void setStrictErrorChecking(boolean strictErrorChecking) {
        dom.setStrictErrorChecking(strictErrorChecking);
    }

    /**
     * @inheritDoc
     */
    public String getDocumentURI() {
        return dom.getDocumentURI();
    }

    /**
     * @inheritDoc
     */
    public void setDocumentURI(String documentURI) {
        dom.setDocumentURI(documentURI);
    }

    /**
     * @inheritDoc
     */
    public Node adoptNode(Node source) throws DOMException {
        return dom.adoptNode(source);
    }

    /**
     * @inheritDoc
     */
    public DOMConfiguration getDomConfig() {
        return dom.getDomConfig();
    }

    /**
     * @inheritDoc
     */
    public void normalizeDocument() {
        dom.normalizeDocument();
    }

    /**
     * @inheritDoc
     */
    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
        return dom.renameNode(n, namespaceURI, qualifiedName);
    }

    /**
     * @inheritDoc
     */
    public String getNodeName() {
        return dom.getNodeName();
    }

    /**
     * @inheritDoc
     */
    public String getNodeValue() throws DOMException {
        return dom.getNodeValue();
    }

    /**
     * @inheritDoc
     */
    public void setNodeValue(String nodeValue) throws DOMException {
        dom.setNodeValue(nodeValue);
    }

    /**
     * @inheritDoc
     */
    public short getNodeType() {
        return dom.getNodeType();
    }

    /**
     * @inheritDoc
     */
    public Node getParentNode() {
        return dom.getParentNode();
    }

    /**
     * @inheritDoc
     */
    public SimpleNodeList getChildNodes() {
        return new SimpleNodeList(dom.getChildNodes());
    }

    /**
     * @inheritDoc
     */
    public Node getFirstChild() {
        return dom.getFirstChild();
    }

    /**
     * @inheritDoc
     */
    public Node getLastChild() {
        return dom.getLastChild();
    }

    /**
     * @inheritDoc
     */
    public Node getPreviousSibling() {
        return dom.getPreviousSibling();
    }

    /**
     * @inheritDoc
     */
    public Node getNextSibling() {
        return dom.getNextSibling();
    }

    /**
     * @inheritDoc
     */
    public NamedNodeMap getAttributes() {
        return dom.getAttributes();
    }

    /**
     * @inheritDoc
     */
    public Document getOwnerDocument() {
        return dom.getOwnerDocument();
    }

    /**
     * @inheritDoc
     */
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return dom.insertBefore(newChild, refChild);
    }

    /**
     * @inheritDoc
     */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return dom.replaceChild(newChild, oldChild);
    }

    /**
     * @inheritDoc
     */
    public Node removeChild(Node oldChild) throws DOMException {
        return dom.removeChild(oldChild);
    }

    /**
     * @inheritDoc
     */
    public Node appendChild(Node newChild) throws DOMException {
        return dom.appendChild(newChild);
    }

    /**
     * @inheritDoc
     */
    public boolean hasChildNodes() {
        return dom.hasChildNodes();
    }

    /**
     * @inheritDoc
     */
    public Node cloneNode(boolean deep) {
        return dom.cloneNode(deep);
    }

    /**
     * @inheritDoc
     */
    public void normalize() {
        dom.normalize();
    }

    /**
     * @inheritDoc
     */
    public boolean isSupported(String feature, String version) {
        return dom.isSupported(feature, version);
    }

    /**
     * @inheritDoc
     */
    public String getNamespaceURI() {
        return dom.getNamespaceURI();
    }

    /**
     * @inheritDoc
     */
    public String getPrefix() {
        return dom.getPrefix();
    }

    /**
     * @inheritDoc
     */
    public void setPrefix(String prefix) throws DOMException {
        dom.setPrefix(prefix);
    }

    /**
     * @inheritDoc
     */
    public String getLocalName() {
        return dom.getLocalName();
    }

    /**
     * @inheritDoc
     */
    public boolean hasAttributes() {
        return dom.hasAttributes();
    }

    /**
     * @inheritDoc
     */
    public String getBaseURI() {
        return dom.getBaseURI();
    }

    /**
     * @inheritDoc
     */
    public short compareDocumentPosition(Node other) throws DOMException {
        return dom.compareDocumentPosition(other);
    }

    /**
     * @inheritDoc
     */
    public String getTextContent() throws DOMException {
        return dom.getTextContent();
    }

    /**
     * @inheritDoc
     */
    public void setTextContent(String textContent) throws DOMException {
        dom.setTextContent(textContent);
    }

    /**
     * @inheritDoc
     */
    public boolean isSameNode(Node other) {
        return dom.isSameNode(other);
    }

    /**
     * @inheritDoc
     */
    public String lookupPrefix(String namespaceURI) {
        return dom.lookupPrefix(namespaceURI);
    }

    /**
     * @inheritDoc
     */
    public boolean isDefaultNamespace(String namespaceURI) {
        return dom.isDefaultNamespace(namespaceURI);
    }

    /**
     * @inheritDoc
     */
    public String lookupNamespaceURI(String prefix) {
        return dom.lookupNamespaceURI(prefix);
    }

    /**
     * @inheritDoc
     */
    public boolean isEqualNode(Node arg) {
        return dom.isEqualNode(arg);
    }

    /**
     * @inheritDoc
     */
    public Object getFeature(String feature, String version) {
        return dom.getFeature(feature, version);
    }

    /**
     * @inheritDoc
     */
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return dom.setUserData(key, data, handler);
    }

    /**
     * @inheritDoc
     */
    public Object getUserData(String key) {
        return dom.getUserData(key);
    }
    
}
