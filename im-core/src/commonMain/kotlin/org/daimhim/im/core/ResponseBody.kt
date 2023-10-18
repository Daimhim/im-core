package org.daimhim.im.core

abstract class ResponseBody{
    abstract fun source():ByteArray
    abstract fun string():String
}