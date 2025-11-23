package com.mete0rfish.huge_traffic_test.entity

import com.mete0rfish.huge_traffic_test.repository.CouponRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.PreparedStatement

@Component
class CouponInitializer(
    private val couponRepository: CouponRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val jdbcTemplate: JdbcTemplate
) {

    private val COUNT_KEY = "coupon:issued:count"
    private val USER_KEY = "coupon:issued:user"

    fun initEvent(limitCount: Int) {
        couponRepository.deleteAll();

        val sql = "INSERT INTO coupons (id, issued_date, user_id) VALUES (?, NULL, NULL)"
        jdbcTemplate.batchUpdate(sql, object: BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                ps.setLong(1, (i+1).toLong())
            }

            override fun getBatchSize(): Int  = limitCount
        })

        redisTemplate.delete("coupon:available:ids")
        redisTemplate.delete("coupon:issued:users")

        redisTemplate.executePipelined { connection ->
            val listKey = "coupon:available:ids".toByteArray()
            for (i in 1..limitCount) {
                connection.listCommands().rPush(listKey, i.toString().toByteArray())
            }
            null
        }

        println("이벤트 준비 완료: 쿠폰 $limitCount 장 생성됨.")
    }

    fun initEventOnlyCouponCount(count: Int) {
        couponRepository.deleteAll();

        val sql = "INSERT INTO coupons (id, issued_date, user_id) VALUES (?, NULL, NULL)"
        jdbcTemplate.batchUpdate(sql, object: BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                ps.setLong(1, (i+1).toLong())
            }

            override fun getBatchSize(): Int  = count
        })

        redisTemplate.delete(listOf(COUNT_KEY, USER_KEY))
        redisTemplate.opsForValue().set(COUNT_KEY, "0")

        println("==== 쿠폰 이벤트 초기화 완료 ====")
        println("발행 수량: $count 개")
    }
}
