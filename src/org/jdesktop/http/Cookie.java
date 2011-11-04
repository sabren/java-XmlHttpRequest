/*
 * $Id: Cookie.java 281 2008-03-19 21:13:12Z rbair $
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

/**
 * Doesn't yet support Cookie2.
 * 
 * @author rbair
 */
public class Cookie extends NameValuePair {
    private String comment;
    private String domain;
    private int maxAge=-1; //TODO not sure if this is the right way to indicate no expiration
    private String path;
    private boolean secure;
    private int version;
 
    public void setComment(String comment) {
        String old = getComment();
        this.comment = comment;
        firePropertyChange("comment", old, getComment());
    }

    public String getComment() {
        return comment;
    }
    
    public void setDomain(String domain) {
        String old = getDomain();
        this.domain = domain;
        firePropertyChange("domain", old, getDomain());
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setMaxAge(int age) {
        int old = getMaxAge();
        this.maxAge = age;
        firePropertyChange("maxAge", old, getMaxAge());
    }
    
    public int getMaxAge() {
        return maxAge;
    }
    
    public void setPath(String path) {
        String old = getPath();
        this.path = path;
        firePropertyChange("path", old, getPath());
    }
    
    public String getPath() {
        return path;
    }
    
    public void setSecure(boolean secure) {
        boolean old = isSecure();
        this.secure = secure;
        firePropertyChange("secure", old, isSecure());
    }
    
    public boolean isSecure() {
        return secure;
    }
    
    public void setVersion(int version) {
        int old = getVersion();
        this.version = version;
        firePropertyChange("version", old, getVersion());
    }
    
    public int getVersion() {
        return version;
    }

    public String toString() {
        return "Cookie [" +
                getName() + "=" + getValue() + ", " +
                "Comment=" + getComment() + ", " +
                "Domain=" + getDomain() + ", " +
                "Max-Age=" + getMaxAge() + ", " +
                "Path=" + getPath() + ", " +
                "Secure=" + isSecure() + ", " +
                "Version=" + getVersion() + "]";
    }
    
    public static Cookie parseCookie(String s) throws RuntimeException {
        Cookie cookie = new Cookie();
        
        if (s == null) {
            throw new NullPointerException("Cannot parse a null value");
        }
        
        //If I knew regex better, this might be more concise.
        //Simply iterate, and pull out each avpair, where they are separated by
        //semi-colons (but, not semi's within quotes).
        
        boolean first = true; //this is the first av-pair being parsed off.
        boolean inQuotes = false; //tracks whether we are in quotes
        int start = 0; //tracks the start of the av pair
        int end = -1; //tracks the end of the av pair
        int equals = -1;
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == '=' && !inQuotes && equals==-1) { //equals==-1 exists so that only the first = sign is used as the split
                //found the split point
                equals = i;
            } else if (c == ';' && !inQuotes) {
                //found the end
                end = i;
                handle(cookie, s, start, equals, end, first);
                start = end + 1;
                equals = -1;
                first = false;
            }
        }
            
        //capture the last item
        if (start >= 0) {
            handle(cookie, s, start, equals, s.length(), first);
        }
        
        return cookie;
    }
    
    private static void handle(Cookie c, String s, int start, int equals, int end, boolean first) throws RuntimeException {
        String name = null, value=null;
        if (equals > -1) {
            name = s.substring(start, equals).trim();
            value = s.substring(equals+1, end).trim();
        } else {
            name = s.substring(start, end).trim();
        }
        
        //if the value has quotes at the start and end, remove them
        if (value != null && value.charAt(0) == '"' && value.charAt(value.length()-1) == '"') {
            value = value.substring(1, value.length() - 1);
        }
        
        if (first) {
            if (name.startsWith("$")) {
                throw new RuntimeException("The firs av-pair cannot begin with a $");
            }
            c.setName(name);
            c.setValue(value);
        } else if ("Comment".equalsIgnoreCase(name)) {
            c.setComment(value);
        } else if ("Domain".equalsIgnoreCase(name)) {
            if (!value.startsWith(".")) {
                value = "." + value;
            }
            c.setDomain(value);
        } else if ("Max-Age".equalsIgnoreCase(name)) {
            int age = Integer.parseInt(value);
            if (age < 0) {
                throw new RuntimeException("Max age must be non-negative");
            }
            c.setMaxAge(age);
        } else if ("Path".equalsIgnoreCase(name)) {
            c.setPath(value);
        } else if ("Secure".equalsIgnoreCase(name)) {
            c.setSecure(true);
        } else if ("Version".equalsIgnoreCase(name)) {
            c.setVersion(Integer.parseInt(value));
        } else if ("Expires".equalsIgnoreCase(name)) {
            System.err.println("Expires not yet handled");
        } else if ("Discard".equalsIgnoreCase(name)) {
            System.err.println("Discard not yet handled");
        } else if ("Port".equalsIgnoreCase(name)) {
            System.err.println("Port not yet handled");
        } else if ("CommentURL".equalsIgnoreCase(name)) {
            System.err.println("CommentURL not yet handled");
        } else {
            System.err.println("Warning: Skipping " + name + "=" + value);
        }
    }
}
