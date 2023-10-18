package org.daimhim.im.core

import android.os.Bundle
import org.daimhim.imc_core.IMCStatusListener

class NativeIMCStatusListener(private val listener: RemoteIMCStatusListener?) : IMCStatusListener {
    override fun connectionClosed() {
        listener?.connectionClosed()
    }

    override fun connectionLost(throwable: Throwable) {
        listener?.connectionLost(Bundle().apply {
            putString("throwable",throwable.toString())
        })
    }

    override fun connectionSucceeded() {
        listener?.connectionSucceeded()
    }

}