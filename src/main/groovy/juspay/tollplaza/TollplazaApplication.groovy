package juspay.tollplaza

import juspay.tollplaza.domains.ServiceApi
import juspay.tollplaza.domains.Tenant
import juspay.tollplaza.services.ThrottleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.task.TaskExecutor
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@SpringBootApplication
class TollplazaApplication {

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        def redisConfig = new RedisStandaloneConfiguration("localhost", 6379)
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory(redisConfig)
        return jedisConFactory
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor()
        executor.setCorePoolSize(50)
        executor.setMaxPoolSize(100)
        executor.setQueueCapacity(10000)
        return executor
    }

    static void main(String[] args) {
		SpringApplication.run TollplazaApplication, args
	}

}
