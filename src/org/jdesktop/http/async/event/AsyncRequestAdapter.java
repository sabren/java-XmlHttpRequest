/*
 * AsyncRequestEvent.java
 *
 * Created on December 19, 2006, 12:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jdesktop.http.async.event;

/**
 *
 * @author rbair
 */
public class AsyncRequestAdapter implements AsyncRequestListener {
    public void onLoad() {}
    public void onError() {}
    public void onProgress() {}
    public void onAbort() {}
    public void onTimeout() {}
}
