package juspay.tollplaza.domains

import javax.xml.ws.Service

class ServiceApi {
    String id
    String name
    String requestPath

    static List<ServiceApi> getServiceApis() {
        return [
                CARD_LIST,
                new ServiceApi("id": "cards/add", "name": "add card", requestPath: "/cards/add"),
                new ServiceApi("id": "cards/delete", "name": "delete card", requestPath: "/cards/delete"),
        ]
    }

    static ServiceApi CARD_LIST = new ServiceApi("id": "cards/list", "name": "list cards", requestPath: "/cards/list")

    @Override
    public String toString() {
        return "ServiceApi{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", requestPath='" + requestPath + '\'' +
                '}';
    }
}
