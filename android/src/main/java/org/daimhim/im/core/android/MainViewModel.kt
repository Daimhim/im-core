package org.daimhim.im.core.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.daimhim.im.core.AndroidIEngine
import org.daimhim.imc_core.*
import timber.multiplatform.log.Timber

class MainViewModel : ViewModel() {

    private val _IMCStatus = MutableStateFlow(IEngineState.ENGINE_CLOSED)
    val imcStatus : StateFlow<Int> =_IMCStatus

    private val _onMessage = MutableSharedFlow<MainItem>()
    val onMessage : SharedFlow<MainItem> =_onMessage

    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiIxMTk5MjQ0MzIxMjU0MTUwMTQ0Iiwic2NvcGUiOiJkZWZhdWx0IiwiaXNzIjoiMTMwODg5NTYxMTIiLCJsb2dpbiI6MTY5MDUyNTI3M30.SVbpqPluVnAoFf_tauafCabh-RfTcXslFD_C95aOKBDfoMM_yOBM16L6Y17q0EpL-eBZc6oi0RQWpAQUYKVbgdboEq3ZFItAMSEphJENcLLKgyy8PVw5cIlNapa9Eq3-wArZHI2qc3ICsR6_FJqH5rEnir6jqXPEqJMdhPvoDkg"
    val imAccount = "202206211949282"
    val base_url = "wss://client.qgbtech.cn/ws:90?token=%s&name=%s&platform=android"


    private var iEngine = AndroidIEngine()

    fun login(name:String){
        viewModelScope
            .launch(Dispatchers.IO) {
                try {
                    iEngine.engineOn(String.format(base_url, token, imAccount))
                    iEngine
                        .setIMCStatusListener(object : IMCStatusListener {
                            override fun connectionClosed() {
                                Timber.i("connectionClosed")
                                viewModelScope
                                    .launch {
                                        _IMCStatus.emit(IEngineState.ENGINE_CLOSED)
                                    }.start()
                            }

                            override fun connectionLost(throwable: Throwable) {
                                Timber.i(throwable,"connectionLost")
                                viewModelScope
                                    .launch {
                                        _IMCStatus.emit(IEngineState.ENGINE_FAILED)
                                    }.start()
                            }

                            override fun connectionSucceeded() {
                                Timber.i("connectionSucceeded")
                                viewModelScope
                                    .launch {
                                        _IMCStatus.emit(IEngineState.ENGINE_OPEN)
                                    }.start()
                            }

                        })
                    iEngine
                        .addIMCListener(object : V2IMCListener {
                            override fun onMessage(text: String) {
                                Timber.i("onMessage:收到新消息 ${text.length} ${System.currentTimeMillis()}")
                                viewModelScope
                                    .launch {
                                        _onMessage.emit(MainItem("Ta",1,text))
                                    }.start()
                            }

                            override fun onMessage(byteArray: ByteArray) {
                                Timber.i("onMessage:收到新消息11  ${byteArray.size} ${System.currentTimeMillis()}")
                                viewModelScope
                                    .launch {
//                            val parseDelimitedFrom =
//                                ChatMessage.parseFrom(byteArray.toByteArray())
                                        Timber.i("onMessage:收到新消息22  ${System.currentTimeMillis()}")
//                            _onMessage.emit(MainItem("Ta",1,parseDelimitedFrom.content))
                                    }.start()
                            }
                        })
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            .start()

    }
    fun send(name: String, text: String) {
        Timber.i("send",iEngine
            .send(text))
    }
    fun loginOut(){
        viewModelScope
            .launch(Dispatchers.IO) {
                iEngine.engineOff()
            }.start()
    }

    fun setForeground(foreground:Boolean){
        viewModelScope
            .launch(Dispatchers.IO) {
                iEngine.onChangeMode(if (foreground) 0 else 1)
            }.start()
    }
    fun onNetworkChange(networkState:Int){
        viewModelScope
            .launch(Dispatchers.IO) {
                iEngine.onNetworkChange(networkState)
            }.start()
    }
}