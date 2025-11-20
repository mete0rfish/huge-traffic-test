package com.mete0rfish.huge_traffic_test.exception

import org.springframework.web.bind.annotation.RestControllerAdvice

class CouponEventFinishedException(message: String) : RuntimeException(message)
