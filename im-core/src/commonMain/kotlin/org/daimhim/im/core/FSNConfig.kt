package org.daimhim.im.core

import org.daimhim.imc_core.OkhttpIEngine


interface FSNConfig {
    companion object {
        internal var fsnConfig: FSNConfig? = null

        @JvmStatic
        fun setFSNConfig(config: FSNConfig) {
            fsnConfig = config
        }

        private val sharedParameters = mutableMapOf<String,String>()
        @JvmStatic
        fun getSharedParameters(key:String):String?{
            return sharedParameters[key]
        }
        @JvmStatic
        fun setSharedParameters(parameters:MutableMap<String,String>){
            for (parameter in parameters) {
                if (sharedParameters.containsKey(parameter.key) && parameter.value.isEmpty()){
                    sharedParameters.remove(parameter.key)
                }else{
                    sharedParameters.put(parameter.key,parameter.value)
                }
            }
            sharedParameters.putAll(parameters)
        }
    }
    fun crate(builder: OkhttpIEngine.Builder): OkhttpIEngine.Builder
    fun bindEngine(okhttpIEngine:OkhttpIEngine)
}