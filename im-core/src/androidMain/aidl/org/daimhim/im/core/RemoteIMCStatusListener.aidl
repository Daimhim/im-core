package org.daimhim.im.core;

interface RemoteIMCStatusListener {
    void connectionClosed();
    void connectionLost(in Bundle throwable);
    void connectionSucceeded();
}