package org.daimhim.im.core

class ByteArrayRequest(
    private val requestId:Long,
    val byteArray: ByteArray,
) : Request {
    override fun requestId(): Long = requestId
    override fun timeOut(): Long {
        return 0
    }
}