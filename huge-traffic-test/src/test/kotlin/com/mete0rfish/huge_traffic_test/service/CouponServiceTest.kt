package com.mete0rfish.huge_traffic_test.service

import com.mete0rfish.huge_traffic_test.repository.CouponRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.Test

@SpringBootTest
class CouponServiceTest @Autowired constructor(
    private val couponService: CouponService,
    private val couponRepository: CouponRepository
) {

    @AfterEach
    fun tearDown() {
        couponRepository.deleteAll()
    }

    @Test
    fun `선착순 100명에게 쿠폰을 발급할 때 동시성 문제가 발생한다`() {
        // given
        val threadCount = 1000
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            val userId = i.toLong()
            executorService.submit {
                try {
                    couponService.issue(userId)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        // then
        val count = couponRepository.count()
        println("count: $count")
        assertThat(count).isNotEqualTo(100)
    }

    @Test
    fun `synchronized 사용 시, 동시성 문제가 해결된다`() {
        // given
        val threadCount = 1000
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            val userId = i.toLong()
            executorService.submit {
                try {
                    couponService.issue(userId)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        // then
        val count = couponRepository.count()
        println("count: $count")
        assertThat(count).isEqualTo(100)
    }
}