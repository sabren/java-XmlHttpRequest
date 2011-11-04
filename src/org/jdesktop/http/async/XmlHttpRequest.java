/*
 * $Id: XmlHttpRequest.java 158 2006-12-20 01:28:38Z rbair $
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

package org.jdesktop.http.async;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.jdesktop.http.Method;
import org.jdesktop.dom.SimpleDocument;
import org.jdesktop.dom.SimpleDocumentBuilder;
import org.jdesktop.http.async.AsyncHttpRequest.ReadyState;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Mimics the AJAX equivilent. The idea is that this class will
 * make a request and get as a response some valid XML.
 * 
 * @author rbair
 */
public class XmlHttpRequest extends AsyncHttpRequest {
    //responseXML: DOM-compatible document object of data returned from server process
    private SimpleDocument responseXML;
    
    /** Creates a new instance of XmlHttpRequest */
    public XmlHttpRequest() {
    }

    /**
     * If the readyState attribute has a value other than LOADED, then this method
     * will return null. Otherwise, if the Content-Type contains text/xml, application/xml,
     * or ends in +xml then a Document will be returned. Otherwise, null is returned.
     */
    public final SimpleDocument getResponseXML() {
        if (getReadyState() == ReadyState.LOADED) {
            return responseXML;
        } else {
            return null;
        }
    }
    
    protected void reset() {
        setResponseXML(null);
        super.reset();
    }
    
    protected void handleResponse(String responseText) throws Exception {
        if (responseText == null) {
            setResponseXML(null);
        } else {
            try {
                setResponseXML(SimpleDocumentBuilder.simpleParse(responseText));
            } catch (Exception e) {
                setResponseXML(null);
                throw e;
            }
        }
    }

    private void setResponseXML(SimpleDocument dom) {
        Document old = this.responseXML;
        this.responseXML = dom;
        firePropertyChange("responseXML", old, this.responseXML);
    }
    
    public static void main(String[] args) {
        final XmlHttpRequest req = new XmlHttpRequest();
        req.addReadyStateChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() == ReadyState.LOADED) {
                    Document dom = req.getResponseXML();
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    try {
                        XPathExpression exp = xpath.compile("//p");
                        NodeList nodes = (NodeList)exp.evaluate(dom, XPathConstants.NODESET);
                        for (int i=0; i<nodes.getLength(); i++) {
                            System.out.println(nodes.item(i).getTextContent());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        try {
            req.open(Method.GET, "http://validator.w3.org/");
            req.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}