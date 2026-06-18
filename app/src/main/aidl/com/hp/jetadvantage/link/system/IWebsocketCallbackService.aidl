// IWebsocketCallbackService.aidl
package com.hp.jetadvantage.link.system;
import com.hp.jetadvantage.link.system.IWebsocketCallback;
// Declare any non-default types here with import statements

interface IWebsocketCallbackService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean addCallback(IWebsocketCallback callback, String target);
    boolean removeCallback(IWebsocketCallback callback, String target);
    void sendMessage(int what, String data);
}
