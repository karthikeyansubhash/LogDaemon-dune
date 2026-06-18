// IWebsocketCallback.aidl
package com.hp.jetadvantage.link.system;

// Declare any non-default types here with import statements

interface IWebsocketCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onMessageReceived(int what, String data);
}
