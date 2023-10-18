package org.daimhim.im.core


class RealCall(
    private val originalRequest: Request,
    private val messagePowerCore: MessagePowerCore,
    private val interceptors:MutableList<Interceptor>,
):Call{
    override fun enqueue(responseCallback: Callback) {
        getResponseWithInterceptorChain(responseCallback)
    }
    private fun getResponseWithInterceptorChain(responseCallback: Callback){
        val mutableListOf = mutableListOf<Interceptor>()
        mutableListOf += interceptors
        //真实的发送者
        mutableListOf += RealSendInterceptor(messagePowerCore, responseCallback)

        val realInterceptorChain = RealInterceptorChain(
            mutableListOf,
            0,
            originalRequest
        )
        realInterceptorChain.proceed(originalRequest)
    }

    override fun cancel() {
        messagePowerCore.cancelRequest(originalRequest.requestId())
    }

}