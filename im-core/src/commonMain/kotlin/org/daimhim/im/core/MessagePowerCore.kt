package org.daimhim.im.core

import org.daimhim.im.core.RequestErrorCode.REQUEST_CANCEL
import org.daimhim.im.core.RequestErrorCode.REQUEST_FAILED_CONNECTING
import org.daimhim.im.core.RequestErrorCode.REQUEST_NOT_SENT
import org.daimhim.im.core.RequestErrorCode.REQUEST_NO_ACKNOWLEDGMENT
import org.daimhim.im.core.RequestErrorCode.REQUEST_SUCCESSFUL
import org.daimhim.im.core.RequestErrorCode.REQUEST_TIMED_OUT
import timber.multiplatform.log.Timber
import java.lang.ref.SoftReference
import java.util.Date
import java.util.concurrent.*

class MessagePowerCore:Runnable {
    internal var mainExecutive :MainExecutive? = null

    private val taskQueue = CopyOnWriteArrayList<Pending>()

    private val responseBodies = ConcurrentHashMap<Long,ResponseBody>()
    private val cancelList = CopyOnWriteArrayList<Long>()

    private val executorService = Executors.newSingleThreadExecutor()


    private var lastTime = System.currentTimeMillis()
    private val lock = Object()

    /**
     * Loop
     *  0. 检查是否有回执
     *  1. 检查超时，是的话 回调回去
     *  2. 获取距离超时最近的一个时间
     *
     */
    override fun run(){
        var intervals : Long
        var iterator:MutableIterator<Pending>
        var next: Pending
        var realSend: Int
        var responseBody:ResponseBody?
        var minTimeout: Long
        while (taskQueue.isNotEmpty()){
            iterator = taskQueue.iterator()
            minTimeout = taskQueue.first().timing
            while (iterator.hasNext()){
                next = iterator.next()
                // 取消处理
                if (cancelList.contains(next.request.requestId())){
                    realFailureCall(next,Response(next.request,REQUEST_CANCEL),
                        IllegalStateException("request was canceled"))
                    iterator.remove()
                    continue
                }
                // 执行立即执行的
                if (next.tryCount <= 0){
                    try {
                        realSend = realSend(next.request)
                    }catch (e:Exception){
                        e.printStackTrace()
                        realFailureCall(
                            next,
                            Response(next.request,REQUEST_FAILED_CONNECTING),e)
                        iterator.remove()
                        continue
                    }
                    if (realSend <= 0){
                        realFailureCall(
                            next,
                            Response(next.request,REQUEST_NOT_SENT),
                            IllegalStateException("Send receipt cannot be less than 0"))
                        iterator.remove()
                        continue
                    }
                    next.tryCount += realSend
                    next.timing = next.timeOut
                    if (next.timeOut <= 0) {
                        realSuccessResponseCall(
                            next,
                            Response(
                                next.request,REQUEST_NO_ACKNOWLEDGMENT
                            ))
                        iterator.remove()
                    }
                    continue
                }
                // 回执检测执行
                responseBody = responseBodies[next.request.requestId()]
                if (responseBody != null) {
                    realSuccessResponseCall(
                        next,
                        Response(next.request,REQUEST_SUCCESSFUL,responseBody))
                    responseBodies.remove(next.request.requestId())
                    iterator.remove()
                }
                // 超时检测执行
                intervals = System.currentTimeMillis() - lastTime
                next.timing -= intervals
                //超时
                if (next.timing < 0) {
                    realFailureCall(
                        next,
                        Response(
                            next.request,REQUEST_TIMED_OUT
                        ), TimeoutException("executeWaitRunQueue time out"))
                    iterator.remove()
                    continue
                }
                // 拿到最小的
                if (minTimeout < next.timing) {
                    minTimeout = next.timing
                }
            }
            responseBodies.clear()
            if (minTimeout > 0){
                Timber.i("await $minTimeout  start:${Date()}")
                synchronized(lock){
                    lock.wait(minTimeout)
                }
                Timber.i("await $minTimeout  end:${Date()}")
            }
            lastTime = System.currentTimeMillis()
        }
    }


    private fun realSend(request: Request):Int{
        mainExecutive?:throw IllegalStateException("platformIEngine can not be null")
        return mainExecutive?.realSend(request)?:-1
    }


    private fun realSuccessResponseCall(pending: Pending,response: Response){
        mainExecutive?.realSuccessResponseCall(pending, response)
    }

    private fun realFailureCall(pending: Pending,response:Response,throwable: Throwable){
        mainExecutive?.realFailureCall(pending, response,throwable)
    }

    fun addRequest(request:Request,callback: Callback){
        taskQueue.add(
            Pending(
                tryCount = 0,
                timing = 0L,
                timeOut = request.timeOut(),
                request = request,
                callback = SoftReference(callback),
            )
        )
        start()
    }

    fun cancelRequest(requestId:Long){
        cancelList.add(requestId)
        start()
    }

    fun callResponseBody(responseId:Long,responseBody:ResponseBody){
        responseBodies[responseId] = responseBody
        start()
    }
    private var future : Future<*>? = null
    private fun start(){
        if (future == null){
            future = executorService.submit(this)
        }
        synchronized(lock){
            lock.notify()
        }
    }

    class Pending(
        var tryCount: Int,
        var timing: Long,
        val timeOut: Long,
        val request: Request,
        val callback: SoftReference<Callback>,
    )

    interface MainExecutive{
        /**
         * Real send
         *
         * @param request
         * @return
         */
        fun realSend(request: Request):Int
        fun realSuccessResponseCall(pending: Pending,response: Response)

        fun realFailureCall(pending: Pending,response:Response,throwable: Throwable)
    }
}