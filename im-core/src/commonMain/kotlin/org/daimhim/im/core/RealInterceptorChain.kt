package org.daimhim.im.core


class RealInterceptorChain(
    private val interceptors: List<Interceptor>,
    private val index: Int,
    private val request: Request,
) : Interceptor.Chain {
    private fun copy(
        index: Int = this.index,
        request: Request = this.request,
    ) = RealInterceptorChain(interceptors, index, request)

    override fun request(): Request {
        return request
    }

    override fun proceed(request: Request){
        val next = copy(index = index + 1, request = request)
        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }
}