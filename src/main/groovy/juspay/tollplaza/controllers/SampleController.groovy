package juspay.tollplaza.controllers

import io.micrometer.core.instrument.MeterRegistry
import juspay.tollplaza.domains.ServiceApi
import juspay.tollplaza.domains.Tenant
import juspay.tollplaza.domains.ThrottleRule
import juspay.tollplaza.services.ThrottleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.*
import org.springframework.core.task.TaskExecutor
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableAutoConfiguration
public class SampleController {

    @Autowired
    ThrottleService throttleService

    @Autowired
    TaskExecutor taskExecutor

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!"
    }

    @RequestMapping("/produce")
    @ResponseBody
    Map<String, String> produceData(@RequestParam Map<String, String> params) {
        produce(Integer.valueOf(params.times?:"10"))
        HashMap<String, String> map = new HashMap<>()
        map.put("key", "value")
        map.put("foo", "bar")
        map.put("aa", "bb")
        return map
    }

    @RequestMapping("/produceAsync")
    @ResponseBody
    String produceDataAsync(@RequestParam Map<String, String> params) {
        produceAsync(Integer.valueOf(params.times?:"10"))
        return "Hello World!"
    }

    def withInstrument = {String name, cls ->
        long start = System.currentTimeMillis()
        cls.call()
        long end = System.currentTimeMillis()
//        println "${name} took ${end - start}ms"
    }

    def produce(int times = 10) {
        withInstrument "produce-sync", {
            for(int i=0;i < times;i++) {
                throttleService.recordServiceInvocation(Tenant.FC, ServiceApi.CARD_LIST)
            }
        }

    }

    def produceAsync(int times = 10) {
        def task = new Runnable() {
            @Override
            void run() {
                withInstrument "produce-async", {
                    for(int i=0;i < times;i++) {
                        throttleService.recordServiceInvocation(Tenant.FC, ServiceApi.CARD_LIST)
                    }
                }
            }
        }
        taskExecutor.execute(task)
    }

    @RequestMapping("/showStatus")
    @ResponseBody
    String getStatus() {
        def conn = jedisConnectionFactory.getConnection()
        return Tenant.FC.getRulesForTenant().collect {ThrottleRule rule->
            def val = conn.get(rule.redisKeyForCount.bytes)
            int currentCount = val ? Integer.valueOf(new String(val)) : 0
            Date lastChangedDate = throttleService.getLastChangedDate(conn, rule)
            return ["${rule.redisKeyForCount}: ${currentCount}", "Last Changed: ${lastChangedDate}"].join(", ")
        }.join("\n")
    }

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory

    @Autowired
    private MeterRegistry registry


}