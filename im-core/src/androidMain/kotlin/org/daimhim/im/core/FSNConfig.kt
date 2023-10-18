package org.daimhim.im.core

import org.daimhim.imc_core.WebSocketEngine

interface FSNConfig {
    fun getBaseWs(token: String?, imAccount: String?):String
    fun getToken():String
    fun getImAccount():String
    fun crate(builder: WebSocketEngine.Builder):WebSocketEngine.Builder
    fun bindEngine(webSocketEngine:WebSocketEngine)
}