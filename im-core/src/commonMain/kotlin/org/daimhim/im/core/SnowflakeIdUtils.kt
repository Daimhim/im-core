package org.daimhim.im.core

class SnowflakeIdUtils(workerId: Long, datacenterId: Long) {
    // ==============================Fields===========================================
    /** 开始时间截 (2015-01-01)  */
    private val twepoch = 1420041600000L

    /** 机器id所占的位数  */
    private val workerIdBits = 5L

    /** 数据标识id所占的位数  */
    private val datacenterIdBits = 5L

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)  */
    private val maxWorkerId = -1L xor (-1L shl workerIdBits.toInt())

    /** 支持的最大数据标识id，结果是31  */
    private val maxDatacenterId = -1L xor (-1L shl datacenterIdBits.toInt())

    /** 序列在id中占的位数  */
    private val sequenceBits = 12L

    /** 机器ID向左移12位  */
    private val workerIdShift = sequenceBits

    /** 数据标识id向左移17位(12+5)  */
    private val datacenterIdShift = sequenceBits + workerIdBits

    /** 时间截向左移22位(5+5+12)  */
    private val timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits

    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)  */
    private val sequenceMask = -1L xor (-1L shl sequenceBits.toInt())

    /** 工作机器ID(0~31)  */
    private val workerId: Long

    /** 数据中心ID(0~31)  */
    private val datacenterId: Long

    /** 毫秒内序列(0~4095)  */
    private var sequence = 0L

    /** 上次生成ID的时间截  */
    private var lastTimestamp = -1L
    //==============================Constructors=====================================
    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    init {
        require(!(workerId > maxWorkerId || workerId < 0)) {
            String.format(
                "worker Id can't be greater than %d or less than 0",
                maxWorkerId
            )
        }
        require(!(datacenterId > maxDatacenterId || datacenterId < 0)) {
            String.format(
                "datacenter Id can't be greater than %d or less than 0",
                maxDatacenterId
            )
        }
        this.workerId = workerId
        this.datacenterId = datacenterId
    }
    // ==============================Methods==========================================
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    @Synchronized
    fun nextId(): Long {
        var timestamp = timeGen()

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            timestamp = lastTimestamp + 1
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = sequence + 1 and sequenceMask
            //毫秒内序列溢出
            if (sequence == 0L) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp)
            }
        } else {
            sequence = 0L
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp

        //移位并通过或运算拼到一起组成64位的ID
        return (timestamp - twepoch shl timestampLeftShift.toInt() //
                or (datacenterId shl datacenterIdShift.toInt()) //
                or (workerId shl workerIdShift.toInt()) //
                or sequence)
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected fun tilNextMillis(lastTimestamp: Long): Long {
        var timestamp = timeGen()
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen()
        }
        return timestamp
    }

    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected fun timeGen(): Long {
        return System.currentTimeMillis()
    }
}