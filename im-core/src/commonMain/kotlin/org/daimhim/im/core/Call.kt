package org.daimhim.im.core

interface Call {
    fun enqueue(responseCallback: Callback)
    fun cancel()
}