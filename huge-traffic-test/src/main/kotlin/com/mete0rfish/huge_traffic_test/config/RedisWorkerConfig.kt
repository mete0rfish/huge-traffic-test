package com.mete0rfish.huge_traffic_test.config

import com.mete0rfish.huge_traffic_test.entity.CouponEventListener
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.StringRedisSerializer

@Profile("worker")
@Configuration
class RedisWorkerConfig {

    @PostConstruct
    fun init() {
        println("RedisWorkerConfig initialized!")
    }

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        messageListenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(messageListenerAdapter, ChannelTopic("coupon_issue_event"))
        }
    }

    @Bean
    fun messageListenerAdapter(listener: CouponEventListener): MessageListenerAdapter {
        val adapter = MessageListenerAdapter(listener)
        adapter.setDefaultListenerMethod("onMessage")
        adapter.setSerializer(StringRedisSerializer())
        return adapter
    }
}
