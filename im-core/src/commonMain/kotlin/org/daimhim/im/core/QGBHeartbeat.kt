package org.daimhim.im.core

import org.daimhim.im.core.GZipUtil.unGzipToString
import org.daimhim.imc_core.CustomHeartbeat
import org.daimhim.imc_core.IEngine

class QGBHeartbeat : CustomHeartbeat {
    override fun byteHeartbeat(): ByteArray {
        return byteArrayOf()
    }

    override fun byteOrString(): Boolean {
        return false
    }

    override fun isHeartbeat(iEngine: IEngine, text: String): Boolean {
        return text.contains("HEART_BEAT")
    }

    override fun isHeartbeat(iEngine: IEngine, bytes: ByteArray): Boolean {
        return isHeartbeat(iEngine,bytes.unGzipToString())
    }

    override fun stringHeartbeat(): String {
       return "{\"fromAccount\":{\"accountId\":\"${FSNConfig
           .getSharedParameters("imAccount")}\"},\"toAccount\":{\"accountId\":\"${FSNConfig
               .getSharedParameters("imAccount")}\"},\"cmdType\":\"HEART_BEAT\"}"
    }
}