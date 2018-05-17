package juspay.tollplaza.domains

import juspay.tollplaza.services.ThrottleService

class Tenant {
    String id
    String name

    List<ThrottleRule> getRulesForTenant() {
        return [ThrottleRule.RULE10]
    }



    static List<Tenant> getTenants() {
        return [FC]
    }

    static Tenant FC = new Tenant("id": "fc", "name": "Freecharge");

    @Override
    public String toString() {
        return "Tenant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
