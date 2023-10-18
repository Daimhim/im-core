package org.daimhim.im.core


object MsgIDFactory {
    /**
     * 雪花算法 生成消息的唯一ID flake id utils
     */
    private val snowflakeIdUtils = SnowflakeIdUtils(3,2)
    fun nextId():Long{
        return snowflakeIdUtils.nextId()
    }
}