package org.daimhim.im.core

class Response(
    private val request:Request,
    private val code:Int,
    private val body:ResponseBody? = null,
){
    fun isSuccessful():Boolean = code >= 0
    fun body():ResponseBody? = body
    fun request():Request = request
    fun code():Int = code
}