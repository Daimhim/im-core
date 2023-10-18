// FrogService.aidl
package org.daimhim.im.core;

import org.daimhim.im.core.RemoteV2IMCListener;
import org.daimhim.im.core.RemoteIMCStatusListener;

// Declare any non-default types here with import statements
interface FrogService {
    void addIMCListener(RemoteV2IMCListener listener);
    void removeIMCListener(RemoteV2IMCListener listener);
    void setIMCStatusListener(RemoteIMCStatusListener listener);
    void setAccountInfo(in String token, in String imAccount);
    void makeConnection();
    void onChangeMode(in int mode);
    void onNetworkChange(in int mode);
    boolean sendByte(
        String md5,
        int index,
        int length,
        in byte[] data
    );
    boolean sendString(
        String md5,
        int index,
        int length,
        in byte[] data
    );
    int engineState();
    void loginOut();
}