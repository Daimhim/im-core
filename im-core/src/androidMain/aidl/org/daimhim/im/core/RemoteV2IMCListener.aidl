package org.daimhim.im.core;

interface RemoteV2IMCListener {
    void onMessageByte(
        String md5,
        int index,
        int length,
        in byte[] data
    );
    void onMessageString(
        String md5,
        int index,
        int length,
        in byte[] data
    );
}