package org.daimhim.im.core

import okio.ByteString

class ByteArrayResponseBody(
    private val body : ByteString
) : ResponseBody() {
    override fun source(): ByteArray {
        return body.toByteArray()
    }

    override fun string(): String {
        return body.utf8()
    }
}