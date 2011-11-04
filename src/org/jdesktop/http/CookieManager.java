/*
 * $Id: CookieManager.java 222 2007-03-28 16:01:54Z rbair $
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

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A JVM-wide manager of HTTP Cookies.
 *
 * @author rbair
 */
public class CookieManager extends CookieHandler {
    /** Creates a new instance of CookieManager */
    private CookieManager() {}
    
    //this is in memory -- wiped out on exit from the VM.
    //map of host-to-cookies
    private static Map<String, Set<Wrapper>> cache = new HashMap<String, Set<Wrapper>>();
    
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
//        System.out.println("Get: " + uri.toString());
//        for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
//            System.out.println(entry.getKey());
//            for (String s : entry.getValue()) {
//                System.out.println("\t" + s);
//            }
//        }
        Map<String, List<String>> cookieHeaders = new HashMap<String, List<String>>();
        if (uri == null) {
            return cookieHeaders;
        }
        
        //get all those cookies which path-match this uri.
        Set<Wrapper> cookies = cache.get(uri.getHost());
        if (cookies != null) {
            for (Wrapper w : cookies) {
                if (pathsMatch(uri.getPath(), w.cookie.getPath())) {
                    List<String> pairs = cookieHeaders.get("Cookie");
                    if (pairs == null) {
                        pairs = new ArrayList<String>();
                        cookieHeaders.put("Cookie", pairs);
                    }

                    Cookie c = w.cookie;
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(c.getName()).append("=").append(c.getValue());
//                    buffer.append(";$Version=" + c.getVersion());
//                    if (c.getPath() != null) {
//                        buffer.append(";$Path=" + c.getPath());
//                    }
//                    if (c.getDomain() != null) {
//                        buffer.append(";$Domain=" + c.getDomain());
//                    }

                    pairs.add(buffer.toString());
                }
            }
        }
        
        //NAME=VALUE;$Path=path;$Domain=domain;$Port="port"
        //$Version=value
        return cookieHeaders;
    }
    
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
//        System.out.println("Set: " + uri.toString());
        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            //TODO also support Set-Cookie2
            //TODO also need to inspect the Cache-control header for rules on whether and how to cache the cookie
            //TODO implement the ability to reject Cookies
            if ("Set-Cookie".equalsIgnoreCase(entry.getKey())) {
                Set<Wrapper> cookies = cache.get(uri.getHost());
                if (cookies == null) {
                    cookies = new HashSet<Wrapper>();
                    cache.put(uri.getHost(), cookies);
                }
                for (String v : entry.getValue()) {
                    Cookie c = Cookie.parseCookie(v);
                    Wrapper w = new Wrapper(c);
                    //if the cookie has a max-age of 0, then simply clear the cache
                    //of this cookie.
                    if (c.getMaxAge() == 0) {
                        cookies.remove(w);
                    } else {
                        cookies.add(w);
                    }
                }
            }
        }
    }
    
    public static Cookie[] getCookies(URI uri) {
        return getCookies(uri.getHost());
    }
    
    public static Cookie[] getCookies(String host) {
        Set<Wrapper> cookies = cache.get(host);
        if (cookies == null) {
            return new Cookie[0];
        } else {
            Cookie[] c = new Cookie[cookies.size()];
            int index = 0;
            for (Wrapper w : cookies) {
                c[index++] = w.cookie;
            }
            return c;
        }
    }
    
    public static Cookie[] getCookies() {
        Set<Cookie> cookies = new HashSet<Cookie>();
        for (Map.Entry<String, Set<Wrapper>> entry : cache.entrySet()) {
            if (entry.getValue() != null) {
                for (Wrapper w : entry.getValue()) {
                    cookies.add(w.cookie);
                }
            }
        }
        return cookies.toArray(new Cookie[0]);
    }
    
    /**
     * For two strings that represent paths, p1 and p2, p1 path-matches p2 if
     * p2 is a prefix of p1 (including the case where p1 and p2 string-compare
     * equal). Thus, the string /tec/waldo path-matches /tec.
     */
    private static boolean pathsMatch(String p1, String p2) {
        if ((p1 == null && p2 != null) || (p2 == null && p1 != null)) {
            return false;
        }
        
        if (p1 == null && p2 == null) {
            return true;
        }
        
        //at this point neither p1 nor p2 are null
        if (p1.equals(p2) || p1.startsWith(p2)) {
            return true;
        }
        
       return false; 
    }
    
    public static void install() {
        CookieHandler.setDefault(new CookieManager());
    }
    
    /**
     * A cookie wrapper. How cute.
     * 
     * Wraps cookies that are placed in the in-memory cache, such that cookies
     * are compared based on their getName() property for equality.
     */
    private static final class Wrapper {
        private Cookie cookie;
        private Wrapper(Cookie c) {
            if (c == null) {
                throw new NullPointerException();
            }
            this.cookie = c;
        }
        public boolean equals(Object o) {
            if (o instanceof Wrapper) {
                return cookie.getName().equalsIgnoreCase(((Wrapper)o).cookie.getName());
            }
            return false;
        }
        /**
         * Sorry, I just couldn't help myself.
         */
        public Cookie unwrap() {return cookie;}
        public void eat() {}
        public boolean wasTasty() {return true;}
    }
}