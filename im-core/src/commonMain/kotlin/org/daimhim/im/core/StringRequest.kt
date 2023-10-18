package org.daimhim.im.core

class StringRequest(
    private val requestId:Long,
    val text: String,
) : Request {
    override fun requestId(): Long = requestId
    override fun timeOut(): Long {
        return 0
    }
}