package org.daimhim.im.core

import okio.ByteString
import org.daimhim.im.core.GZipUtil.unGzipToString
import org.daimhim.imc_core.CustomHeartbeat
import org.daimhim.imc_core.IEngine

class QGBHeartbeat : CustomHeartbeat {
    override fun byteHeartbeat(): ByteString {
        return ByteString.EMPTY
    }

    override fun byteOrString(): Boolean {
        return false
    }

    override fun isHeartbeat(iEngine: IEngine, text: String): Boolean {
        return text.contains("HEART_BEAT")
    }

    override fun isHeartbeat(iEngine: IEngine, bytes: ByteString): Boolean {
        return isHeartbeat(iEngine,bytes.unGzipToString())
    }

    override fun stringHeartbeat(): String {
       return "{\"fromAccount\":{\"accountId\":\"${FrogServiceNative.fsnConfig?.getImAccount()}\"},\"toAccount\":{\"accountId\":\"${FrogServiceNative.fsnConfig?.getImAccount()}\"},\"cmdType\":\"HEART_BEAT\"}"
    }
}