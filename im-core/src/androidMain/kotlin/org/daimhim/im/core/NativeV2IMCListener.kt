package org.daimhim.im.core

import okio.ByteString
import org.daimhim.imc_core.V2IMCListener
import timber.multiplatform.log.Timber

class NativeV2IMCListener(private val valueCallBack: RemoteV2IMCListener? = null) : V2IMCListener {

    override fun onMessage(text: String) {
        valueCallBack?:return
        Timber.i("消息传输速度：发 00000")
        BigDataSplitUtil.dataSplitting(text){p0,p1,p2,p3->
            valueCallBack.onMessageString(p0,p1,p2,p3)
        }
    }

    override fun onMessage(byteArray: ByteString) {
        valueCallBack?:return
        Timber.i("消息传输速度：发 00000")
        BigDataSplitUtil.dataSplitting(byteArray.toByteArray()){p0,p1,p2,p3->
            valueCallBack.onMessageByte(p0,p1,p2,p3)
        }
    }
}