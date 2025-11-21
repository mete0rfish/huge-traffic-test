package com.mete0rfish.huge_traffic_test.service

import com.mete0rfish.huge_traffic_test.exception.CouponEventFinishedException
import com.mete0rfish.huge_traffic_test.repository.CouponCountRepository
import com.mete0rfish.huge_traffic_test.repository.CouponRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class CouponRedisService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val couponAssignmentService: CouponAssignmentService,
    private val couponRepository: CouponRepository,
    private val couponCountRepository: CouponCountRepository
) {

    private val COUNT_KEY = "coupon:available:count"
    private val USER_KEY = "coupon:issued:user"

    fun issueWithRedis(userId: Long) {

        val isNewUser = redisTemplate.opsForSet().add(USER_KEY, userId.toString())

        if (isNewUser != 1L) { throw CouponEventFinishedException("이미 발급된 유저입니다.") }

        val remainingCount = redisTemplate.opsForValue().decrement(COUNT_KEY)

        if (remainingCount == null || remainingCount < 0) {
            redisTemplate.opsForValue().increment(COUNT_KEY)
            redisTemplate.opsForSet().remove(USER_KEY, userId.toString())
            throw CouponEventFinishedException("마감 되었습니다.")
        }

        try {
            couponAssignmentService.assignCoupon(userId)

        } catch (e: Exception) {
            redisTemplate.opsForValue().increment(COUNT_KEY)
            redisTemplate.opsForSet().remove(USER_KEY, userId.toString())
            throw e
        }
    }
}