package org.daimhim.im.core.android

import android.app.Application
import com.kongqw.network.monitor.NetworkMonitorManager
import org.daimhim.container.ContextHelper
import org.daimhim.im.core.FSNConfig
import org.daimhim.im.core.QGBHeartbeat
import org.daimhim.imc_core.OkhttpIEngine
import timber.multiplatform.log.DebugTree
import timber.multiplatform.log.Timber

class StartApp : Application() {


    //    private val base_url = "ws://44293ff6.r9.cpolar.top"
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        ContextHelper.init(this)
        val processName = ContextHelper.getProcessName()
        Timber.i("processName:${processName}")
        when(processName){
            "main"->{
                NetworkMonitorManager.getInstance().init(this)
                registerActivityLifecycleCallbacks(FullLifecycleHandler())
            }
            "frog"->{
                FSNConfig.setFSNConfig(object : FSNConfig {

                    override fun crate(builder: OkhttpIEngine.Builder): OkhttpIEngine.Builder {
                        return builder
                            .customHeartbeat(QGBHeartbeat())
                    }

                    override fun bindEngine(webSocketEngine: OkhttpIEngine) {

                    }
                })
            }
        }
    }

}