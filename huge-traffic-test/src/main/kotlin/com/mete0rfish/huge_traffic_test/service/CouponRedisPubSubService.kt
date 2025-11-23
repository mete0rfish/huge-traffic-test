package com.mete0rfish.huge_traffic_test.service

import com.mete0rfish.huge_traffic_test.exception.CouponEventFinishedException
import com.mete0rfish.huge_traffic_test.repository.CouponCountRepository
import com.mete0rfish.huge_traffic_test.repository.CouponRepository
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class CouponRedisPubSubService(
    private val redisTemplate: RedisTemplate<String, String>,
) {

    private val EVENT_CHANNEL = "coupon_issue_event"
    private val COUNT_KEY = "coupon:issued:count"
    private val USER_KEY = "coupon:issued:user"
    private val WORK_QUEUE_KEY = "coupon:work:queue"

    private val LIMIT_COUNT = 100L

    fun issue(userId: Long) {
        val isNewUser = redisTemplate.opsForSet().add(USER_KEY, userId.toString())
        if (isNewUser != 1L) {
            throw CouponEventFinishedException("이미 발급된 유저입니다.")
        }

        val issuedId = redisTemplate.opsForValue().increment(COUNT_KEY) ?: 0L
        if (issuedId > LIMIT_COUNT) {
            redisTemplate.opsForSet().remove(USER_KEY, userId.toString())
            throw CouponEventFinishedException("선착순 마감되었습니다.")
        }

        try {
            val payload = "$userId:$issuedId"
            redisTemplate.opsForList().rightPush(WORK_QUEUE_KEY, payload)
            redisTemplate.convertAndSend(EVENT_CHANNEL, "NEW_JOB")
        } catch (e: Exception) {
            redisTemplate.opsForSet().remove(USER_KEY, userId.toString())
            throw CouponEventFinishedException("[INTERNAL] 시스템 오류로 발급 취소됨. 다시 시도해주세요.")
        }
    }

    fun issueWithPipeline(userId: Long) {
        val results = redisTemplate.executePipelined { connection ->
            val userKeyBytes = USER_KEY.toByteArray()
            val countKeyBytes = COUNT_KEY.toByteArray()
            val userIdBytes = userId.toString().toByteArray()

            connection.setCommands().sAdd(userKeyBytes, userIdBytes)
            connection.stringCommands().incr(countKeyBytes)
            null
        }

        val isNewUser = results[0] as Long
        val issuedId = results[1] as Long

        if (isNewUser == 0L) {
            throw CouponEventFinishedException("이미 발급된 유저입니다.")
        }

        if (issuedId > LIMIT_COUNT) {
            redisTemplate.opsForSet().remove(USER_KEY, userId.toString())
            throw CouponEventFinishedException("마감되었습니다.")
        }

        val payload = "$userId:$issuedId"
        redisTemplate.opsForList().rightPush(WORK_QUEUE_KEY, payload)
        redisTemplate.convertAndSend(EVENT_CHANNEL, "NEW_JOB")
    }
}
