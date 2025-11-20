package com.mete0rfish.huge_traffic_test.repository

import com.mete0rfish.huge_traffic_test.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long>
