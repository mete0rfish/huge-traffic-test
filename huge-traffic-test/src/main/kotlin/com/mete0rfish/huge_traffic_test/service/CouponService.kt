package com.mete0rfish.huge_traffic_test.service

import com.mete0rfish.huge_traffic_test.entity.Coupon
import com.mete0rfish.huge_traffic_test.exception.CouponEventFinishedException
import com.mete0rfish.huge_traffic_test.repository.CouponCountRepository
import com.mete0rfish.huge_traffic_test.repository.CouponRepository
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CouponService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val couponAssignmentService: CouponAssignmentService,
    private val couponRepository: CouponRepository,
    private val couponCountRepository: CouponCountRepository
) {

    @Transactional
    fun issue(userId: Long) {
        val couponCount = couponCountRepository.findByIdWithLock(1L)
            ?: throw CouponEventFinishedException("[Internal] 개수 관리 엔티티가 없습니다.")

        if (couponCount.count >= couponCount.limitCount) {
            throw CouponEventFinishedException("이벤트가 종료되었습니다.")
        }

        couponCount.count += 1
        couponRepository.save(Coupon(userId = userId))
    }

    @Transactional
    fun issueWithLock(userId: Long) {
        val count = couponRepository.countWithLock()

        if (count < 100) {
            couponRepository.save(Coupon(userId = userId, issuedDate = LocalDateTime.now()))
        } else {
            throw CouponEventFinishedException("마감되었습니다.")
        }
    }

}
