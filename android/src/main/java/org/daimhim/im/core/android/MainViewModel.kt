package org.daimhim.im.core.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.ByteString
import org.daimhim.im.core.Callback
import org.daimhim.im.core.PlatformIEngine
import org.daimhim.im.core.Response
import org.daimhim.im.core.android.MainItem
import org.daimhim.imc_core.*
import timber.multiplatform.log.Timber

class MainViewModel : ViewModel() {

    private val _IMCStatus = MutableStateFlow(IEngineState.ENGINE_CLOSED)
    val imcStatus : StateFlow<Int> =_IMCStatus

    private val _onMessage = MutableSharedFlow<MainItem>()
    val onMessage : SharedFlow<MainItem> =_onMessage

    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiIxMTk5MjQ0MzIxMjU0MTUwMTQ0Iiwic2NvcGUiOiJkZWZhdWx0IiwiaXNzIjoiMTMwODg5NTYxMTIiLCJsb2dpbiI6MTY5MDUyNTI3M30.SVbpqPluVnAoFf_tauafCabh-RfTcXslFD_C95aOKBDfoMM_yOBM16L6Y17q0EpL-eBZc6oi0RQWpAQUYKVbgdboEq3ZFItAMSEphJENcLLKgyy8PVw5cIlNapa9Eq3-wArZHI2qc3ICsR6_FJqH5rEnir6jqXPEqJMdhPvoDkg"
    val imAccount = "202206211949282"


    private var iEngine = PlatformIEngine()
    init {
        iEngine.setIMCStatusListener(object : IMCStatusListener {
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

                override fun onMessage(byteArray: ByteString) {
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
    }

    fun login(name:String){
        viewModelScope
            .launch(Dispatchers.IO) {
                try {
                    iEngine.login(token,imAccount)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            .start()

    }
    fun send(name: String, text: String) {
        iEngine
            .send(text)
            .enqueue(object : Callback {
                override fun onFailure(request: org.daimhim.im.core.Request, e: Throwable) {
                    Timber.i(e, "onFailure")
                }

                override fun onResponse(request: org.daimhim.im.core.Request, response: Response) {
                    Timber.i("onResponse")
                }

            })
    }
    fun loginOut(){
        viewModelScope
            .launch(Dispatchers.IO) {
                iEngine.signOut()
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