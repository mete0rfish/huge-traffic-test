package com.mete0rfish.huge_traffic_test.service

import com.mete0rfish.huge_traffic_test.entity.Coupon
import com.mete0rfish.huge_traffic_test.exception.CouponEventFinishedException
import com.mete0rfish.huge_traffic_test.repository.CouponRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponAssignmentService(
    private val couponRepository: CouponRepository,
) {

    @Transactional
    fun assignCouponToUser(couponId: Long, userID: Long) {
        val affected = couponRepository.assignCoupon(couponId, userID)
        if (affected == 0) {
            throw CouponEventFinishedException("[INTERNAL] 쿠폰 저장 중 에러가 발생했습니다.")
        }
    }

    fun assignCoupon(userId: Long) {
        couponRepository.save<Coupon>(
            Coupon(userId = userId)
        )
    }
}