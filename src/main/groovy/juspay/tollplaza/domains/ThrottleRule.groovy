package juspay.tollplaza.domains

import javax.validation.constraints.NotNull


class ThrottleRule {
    Tenant tenant
    ServiceApi serviceApi
    enum DURATION {MINUTE}
    DURATION duration
    int currentCount
    int window
    int threshold

    static ThrottleRule RULE10 = new ThrottleRule(tenant: Tenant.FC,
            serviceApi: ServiceApi.CARD_LIST,
            duration: DURATION.MINUTE,
            window: 1,
            threshold: 10
    )

    boolean doesMatch(@NotNull ServiceApi incomingRequest) {
        assert incomingRequest
        return incomingRequest.requestPath == this.serviceApi.requestPath
    }

    boolean isTimeToClear(Date lastChanged) {
        (System.currentTimeMillis() - lastChanged.getTime()) > window * 60 * 1000
    }

    void restartCounting() {
        Calendar cal = Calendar.getInstance()
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        this.currentCount = 1
    }

    // inferred based on data
    boolean isBreached() {
        return threshold < currentCount
    }

    String getRedisKeyForCount() {
        return this.tenant.id + "_" + this.serviceApi.id
    }

    String getRedisKeyForLastChanged() {
        return this.tenant.id + "_" + this.serviceApi.id + "_" + "last_changed"
    }


}
