package com.mete0rfish.huge_traffic_test.repository

import com.mete0rfish.huge_traffic_test.entity.CouponCount
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface CouponCountRepository : JpaRepository<CouponCount, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CouponCount c where c.id = :id")
    fun findByIdWithLock(id: Long): CouponCount?
}