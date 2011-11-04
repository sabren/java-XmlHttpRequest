/*
 * $Id: SimpleDocumentBuilder.java 240 2007-05-21 21:43:02Z rbair $
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>A DOM {@link javax.xml.parsers.DocumentBuilder} implementation that does
 * not require the factory pattern for creation. Most of the time calling one of
 * the static <code>simpleParse</code> methods is all that is required. Occasionally
 * you may need to create an instance of SimpleDocumentBuilder to tweak some of the
 * builder settings (such as setting an ErrorHandler or EntityResolver).</p>
 * 
 * @author rbair
 */
public class SimpleDocumentBuilder extends DocumentBuilder {
    private static ThreadLocal<SimpleDocumentBuilder> PARSER = new ThreadLocal<SimpleDocumentBuilder>() {
        protected SimpleDocumentBuilder initialValue() {
            return new SimpleDocumentBuilder(false);
        }
    };
    
    private static ThreadLocal<SimpleDocumentBuilder> NS_PARSER = new ThreadLocal<SimpleDocumentBuilder>() {
        protected SimpleDocumentBuilder initialValue() {
            return new SimpleDocumentBuilder(true);
        }
    };
    
    private DocumentBuilder builder;
    
    /**
     * Create a new SimpleDocumentBuilder. SimpleDocumentBuilder will delegate
     * parsing to the default DocumentBuilder constructed via the default
     * DocumentBuilderFactory. This builder factory will be constructed to
     * be namespace aware by default, unlike the traditional Document builders.
     */
    public SimpleDocumentBuilder() {
        this(true);
    }
    
    public SimpleDocumentBuilder(boolean namespaceAware) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(namespaceAware);
            builder = f.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException("Could not create DOM DocumentBuilder", ex);
        }
    }
    
    /**
     * Create a SimpleDocumentBuilder that will wrap builders created from the
     * given factory. This can be used to construct non-namespace aware documents,
     * for example.
     */
    public SimpleDocumentBuilder(DocumentBuilderFactory factory) {
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException("Could not create DOM DocumentBuilder", ex);
        }
    }
    
    /**
     * Parse the content of the given String as an XML
     * document and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * String is null.
     *
     * @param xml String containing the content to be parsed.
     *
     * @return <code>SimpleDocument</code> result of parsing the
     *  String
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>xml</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public SimpleDocument parseString(String xml) throws SAXException, IOException {
        if (xml == null) {
            throw new IllegalArgumentException("xml cannot be null");
        }
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
        return parse(in);
    }
    
    //---------------------------------------------- DocumentBuilder methods
    
    /**
     * @inheritDoc
     */
    public SimpleDocument parse(InputSource is) throws SAXException, IOException {
        return new SimpleDocument(builder.parse(is));
    }

    /**
     * @inheritDoc
     */
    public SimpleDocument parse(InputStream is) throws SAXException, IOException {
        return new SimpleDocument(super.parse(is));
    }

    /**
     * @inheritDoc
     */
    public SimpleDocument parse(InputStream is, String systemId) throws SAXException, IOException {
        return new SimpleDocument(super.parse(is, systemId));
    }

    /**
     * @inheritDoc
     */
    public SimpleDocument parse(String uri) throws SAXException, IOException {
        return new SimpleDocument(super.parse(uri));
    }

    /**
     * @inheritDoc
     */
    public SimpleDocument parse(File f) throws SAXException, IOException {
        return new SimpleDocument(super.parse(f));
    }
    
    /**
     * @inheritDoc
     */
    public boolean isNamespaceAware() {
        return builder.isNamespaceAware();
    }

    /**
     * @inheritDoc
     */
    public boolean isValidating() {
        return builder.isValidating();
    }

    /**
     * @inheritDoc
     */
    public void setEntityResolver(EntityResolver er) {
        builder.setEntityResolver(er);
    }

    /**
     * @inheritDoc
     */
    public void setErrorHandler(ErrorHandler eh) {
        builder.setErrorHandler(eh);
    }

    /**
     * @inheritDoc
     */
    public SimpleDocument newDocument() {
        return new SimpleDocument(builder.newDocument());
    }

    /**
     * @return an unenclosed Document. This is used only by the SimpleDocument
     * no arg constructor
     */
    Document newPlainDocument() {
        return builder.newDocument();
    }
    
    /**
     * @inheritDoc
     */
    public DOMImplementation getDOMImplementation() {
        return builder.getDOMImplementation();
    }

    /**
     * @inheritDoc
     */
    public void reset() {
        builder.reset();
    }
    
    /**
     * @inheritDoc
     */
    public Schema getSchema() {
        return builder.getSchema();
    }
    
    /**
     * @inheritDoc
     */
    public boolean isXIncludeAware() {
        return builder.isXIncludeAware();
    }
    
    //------------------------------------------------------- Static methods
    
    /**
     * Parse the content of the given input source as an XML document
     * and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * <code>InputSource</code> is <code>null</code> null.
     *
     * @param is InputSource containing the content to be parsed.
     *
     * @return A new DOM SimpleDocument object.
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>is</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public static SimpleDocument simpleParse(InputSource is) throws SAXException, IOException {
        return simpleParse(is, false);
    }
    
    /**
     * Parse the content of the given <code>InputStream</code> as an XML
     * document and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * <code>InputStream</code> is null.
     *
     * @param is InputStream containing the content to be parsed.
     *
     * @return <code>SimpleDocument</code> result of parsing the
     *  <code>InputStream</code>
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>is</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public static SimpleDocument simpleParse(InputStream in) throws SAXException, IOException {
        return simpleParse(in, false);
    }
    
    /**
     * Parse the content of the given URL as an XML document
     * and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * URI is <code>null</code> null.
     *
     * @param uri The location of the content to be parsed.
     *
     * @return A new DOM SimpleDocument object.
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>url</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public static SimpleDocument simpleParse(URL url) throws SAXException, IOException {
        return simpleParse(url, false);
    }

    /**
     * Parse the content of the given String as an XML
     * document and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * String is null.
     *
     * @param xml String containing the content to be parsed.
     *
     * @return <code>SimpleDocument</code> result of parsing the
     *  String
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>xml</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public static SimpleDocument simpleParse(String xml) throws SAXException, IOException {
        return simpleParse(xml, false);
    }

    /**
     * Parse the content of the given input source as an XML document
     * and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * <code>InputSource</code> is <code>null</code> null.
     *
     * @param is InputSource containing the content to be parsed.
     * @param namespaceAware whether the parser should be namespace aware
     *
     * @return A new DOM SimpleDocument object.
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>is</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public static SimpleDocument simpleParse(InputSource is, boolean namespaceAware) throws SAXException, IOException {
        return namespaceAware ? 
            (SimpleDocument)NS_PARSER.get().parse(is) :
            (SimpleDocument)PARSER.get().parse(is);
    }
    
    /**
     * Parse the content of the given <code>InputStream</code> as an XML
     * document and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * <code>InputStream</code> is null.
     *
     * @param in InputStream containing the content to be parsed.
     * @param namespaceAware whether the parser should be namespace aware
     *
     * @return <code>SimpleDocument</code> result of parsing the
     *  <code>InputStream</code>
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>is</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public static SimpleDocument simpleParse(InputStream in, boolean namespaceAware) throws SAXException, IOException {
        return namespaceAware ? 
            (SimpleDocument)NS_PARSER.get().parse(in) :
            (SimpleDocument)PARSER.get().parse(in);
    }
    
    /**
     * Parse the content of the given URL as an XML document
     * and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * URI is <code>null</code> null.
     *
     * @param url The location of the content to be parsed.
     * @param namespaceAware whether the parser should be namespace aware
     *
     * @return A new DOM SimpleDocument object.
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>url</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public static SimpleDocument simpleParse(URL url, boolean namespaceAware) throws SAXException, IOException {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        InputStream in = url.openStream();
        return simpleParse(in, namespaceAware);
    }

    /**
     * Parse the content of the given String as an XML
     * document and return a new DOM {@link SimpleDocument} object.
     * An <code>IllegalArgumentException</code> is thrown if the
     * String is null.
     *
     * @param xml String containing the content to be parsed.
     * @param namespaceAware whether the parser should be namespace aware
     *
     * @return <code>SimpleDocument</code> result of parsing the
     *  String
     *
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     * @throws IllegalArgumentException When <code>xml</code> is <code>null</code>
     *
     * @see org.xml.sax.DocumentHandler
     */
    public static SimpleDocument simpleParse(String xml, boolean namespaceAware) throws SAXException, IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
        return simpleParse(in, namespaceAware);
    }
}
