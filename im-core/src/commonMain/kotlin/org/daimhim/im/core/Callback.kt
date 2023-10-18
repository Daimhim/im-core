package org.daimhim.im.core


interface Callback {
    fun onFailure(response: Response, e: Throwable)
    fun onResponse(response: Response)
}