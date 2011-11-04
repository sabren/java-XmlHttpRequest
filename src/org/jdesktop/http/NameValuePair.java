/*
 * $Id: NameValuePair.java 282 2008-03-19 21:13:55Z rbair $
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

package org.jdesktop.http;

import org.jdesktop.beans.AbstractBean;

/**
 * Represents a name/value pair. Both the name and value properties
 * are bound (meaning a PropertyChangeEvent will be fired when their values
 * change).
 * 
 * @author rbair
 */
public class NameValuePair extends AbstractBean implements Cloneable {
    private String name;
    private String value;
    
    /** 
     * Creates a new instance of NameValuePair. Both name and value will
     * be null.
     */
    public NameValuePair() {
    }
    
    /**
     * Creates a new instance of NameValuePair, using the supplied name and value.
     * @param name the name to use. May be null.
     * @param value the value to use. May be null.
     */
    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * Sets the name. A PropertyChangeEvent will be fired if the name is
     * different from the current name value.
     * 
     * @param name The name to use. May be null.
     */
    public void setName(String name) {
        String old = getName();
        this.name = name;
        firePropertyChange("name", old, name);
    }
    
    /**
     * Gets the name.
     * 
     * @return the name. May be null.
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Sets the value. A PropertyChangeEvent will be fired if the value is
     * different from the current value.
     * 
     * @param value The value to use. May be null.
     */
    public void setValue(String value) {
        String old = getValue();
        this.value = value;
        firePropertyChange("value", old, value);
    }
    
    /**
     * Gets the value.
     * 
     * @return the value. May be null.
     */
    public final String getValue() {
        return value;
    }
    
    @Override public NameValuePair clone() {
        return new NameValuePair(name, value);
    }
    
    @Override public String toString() {
        return name + "=" + value;
    }
}
