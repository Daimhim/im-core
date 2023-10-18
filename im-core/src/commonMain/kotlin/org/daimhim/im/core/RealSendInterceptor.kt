package org.daimhim.im.core

class RealSendInterceptor(
    private val messagePowerCore: MessagePowerCore,
    private val responseCallback: Callback
    ) : Interceptor {
    override fun intercept(chain: Interceptor.Chain) {
        messagePowerCore.addRequest(chain.request(),responseCallback)
    }
}