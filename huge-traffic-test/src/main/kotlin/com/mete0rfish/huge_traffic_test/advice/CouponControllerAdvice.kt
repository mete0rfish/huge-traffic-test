package com.mete0rfish.huge_traffic_test.advice

import com.mete0rfish.huge_traffic_test.exception.CouponEventFinishedException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CouponControllerAdvice {

    @ExceptionHandler(CouponEventFinishedException::class)
    @ResponseStatus(HttpStatus.GONE)
    fun handleCouponEventFinishedException(ex: CouponEventFinishedException): String? {
        return ex.message
    }
}
