package org.daimhim.im.core


interface Interceptor {
    fun intercept(chain: Chain)

    interface Chain {
        fun request():Request
        fun proceed(request: Request)
    }
}