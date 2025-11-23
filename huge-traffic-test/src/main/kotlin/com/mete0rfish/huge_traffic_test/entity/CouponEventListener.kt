package com.mete0rfish.huge_traffic_test.entity

import com.mete0rfish.huge_traffic_test.service.CouponAssignmentService
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Profile("worker")
@Component
class CouponEventListener(
    private val redisTemplate: RedisTemplate<String, String>,
    private val couponAssignmentService: CouponAssignmentService
) {

    fun onMessage(message: String, channel: String) {
        val payload = redisTemplate.opsForList().leftPop("coupon:work:queue") ?: return

        val parts = payload.split(":")
        val userId = parts[0].toLong()
        val couponId = parts[1].toLong()

        try {
            couponAssignmentService.assignCouponToUser(couponId, userId)
            println("[Success] User:$userId -> Coupon:$couponId")
        } catch (e: Exception) {
            println("[Error] DB 저장 실패: ${e.message}")
            redisTemplate.opsForSet().remove("coupon:issued:user", userId.toString())
        }
    }
}
