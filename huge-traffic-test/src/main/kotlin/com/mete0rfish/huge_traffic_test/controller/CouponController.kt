package com.mete0rfish.huge_traffic_test.controller

import com.mete0rfish.huge_traffic_test.entity.CouponInitializer
import com.mete0rfish.huge_traffic_test.repository.CouponRepository
import com.mete0rfish.huge_traffic_test.service.CouponRedisService
import com.mete0rfish.huge_traffic_test.service.CouponRedisPubSubService
import com.mete0rfish.huge_traffic_test.service.CouponService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/coupons")
class CouponController(
    private val couponService: CouponService,
    private val couponRedisService: CouponRedisService,
    private val couponRedisPubSubService: CouponRedisPubSubService,
    private val couponRepository: CouponRepository,
    private val couponInitializer: CouponInitializer
) {

    @PostMapping("/issue-lock")
    fun issueWithLock(@RequestParam userId: Long): String {
        couponService.issue(userId)
        return "발급 요청 완료"
    }

    @PostMapping("/issue-redis")
    fun issueWithRedis(@RequestParam userId: Long): String {
        couponRedisService.issueWithRedis(userId)
        return "발급 요청 완료"
    }

    @PostMapping("/issue-redis-stream")
    fun issueWithQueue(@RequestParam userId: Long): String {
        couponRedisPubSubService.issueWithPipeline(userId)
        return "발급 요청 완료"
    }

    @DeleteMapping("/reset")
    fun reset(): String {
        couponRepository.deleteAll()
        return "데이터 초기화 완료"
    }

    @GetMapping("/init")
    fun initCoupons() {
        couponInitializer.initEventOnlyCouponCount(100)
    }
}
