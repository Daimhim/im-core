package org.daimhim.im.core

class StringResponseBody(
    private val body :String
) : ResponseBody() {
    override fun source(): ByteArray {
        return body.toByteArray(Charsets.UTF_8)
    }

    override fun string(): String {
        return body
    }

}