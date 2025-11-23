package com.mete0rfish.huge_traffic_test.service

import com.mete0rfish.huge_traffic_test.entity.Coupon
import com.mete0rfish.huge_traffic_test.repository.CouponRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponAssignmentService(
    private val couponRepository: CouponRepository,
) {

    @Transactional
    fun assignCouponToUser(couponId: Long, userID: Long) {
        couponRepository.assignCoupon(couponId, userID)
    }

    fun assignCoupon(userId: Long) {
        couponRepository.save<Coupon>(
            Coupon(userId = userId)
        )
    }
}