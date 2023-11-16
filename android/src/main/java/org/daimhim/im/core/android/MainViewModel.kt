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
import org.daimhim.im.core.FSNConfig
import org.daimhim.imc_core.*
import timber.multiplatform.log.Timber

class MainViewModel : ViewModel() {

    private val _IMCStatus = MutableStateFlow(IEngineState.ENGINE_CLOSED)
    val imcStatus : StateFlow<Int> =_IMCStatus

    private val _onMessage = MutableSharedFlow<MainItem>()
    val onMessage : SharedFlow<MainItem> =_onMessage

    val token = ""
    val imAccount = ""
    val base_url = ""


    private var iEngine = AndroidIEngine()

    fun login(name:String){
        viewModelScope
            .launch(Dispatchers.IO) {
                try {
                    iEngine
                        .setSharedParameters(
                            mutableMapOf(
                                "token" to "",
                                "imAccount" to ""
                            )
                        )
                    iEngine.engineOn(base_url)
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