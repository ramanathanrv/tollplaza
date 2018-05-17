package juspay.tollplaza.services

import juspay.tollplaza.domains.ServiceApi
import juspay.tollplaza.domains.Tenant
import juspay.tollplaza.domains.ThrottleRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.stereotype.Service

@Service
class ThrottleService {

    public void init() {

    }

    @Autowired
    JedisConnectionFactory jedisConnectionFactory

    public Date getLastChangedDate(def conn, ThrottleRule rule) {
        byte[] lastChanged = conn.get(rule.redisKeyForLastChanged.bytes)
        if(lastChanged == null) return new Date(0)
        return new Date(Long.valueOf(new String(lastChanged)))
    }

    public void recordServiceInvocation(Tenant tenant, ServiceApi serviceApi) {

//        println "processing call: ${serviceApi} for tenant: ${tenant}"
        tenant.getRulesForTenant().each { ThrottleRule rule ->
            if(rule.doesMatch(serviceApi)) {
//                println "rule matches"
                def conn = jedisConnectionFactory.getConnection()
                try {
                    Date lastChangedDate = getLastChangedDate(conn, rule)
                    boolean hasWindowMoved = rule.isTimeToClear(lastChangedDate)
                    def restartCounting = {
                        Calendar cal = Calendar.getInstance()
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        return [cal.getTime(), 1]
                    }
                    if(hasWindowMoved) {
                        def (newLastChanged, newCount) = restartCounting()
                        System.out.print([newLastChanged, newCount].join(" ") + "\n")
                        conn.set(rule.redisKeyForCount.bytes, String.valueOf(newCount).bytes)
                        conn.set(rule.redisKeyForLastChanged.bytes, String.valueOf(newLastChanged.getTime()).bytes)
                    }
                    else {
                        conn.incr(rule.redisKeyForCount.bytes)
                    }
                } catch(Throwable e) {
                    println e.printStackTrace()
                } finally {
                    conn.close()
                }
            }
        }
    }

}
