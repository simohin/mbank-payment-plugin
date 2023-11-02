package common.config;


import common.service.RequestBuildingService;

public final class RequestBuilderConfig {
    private static RequestBuildingService instance;

    private RequestBuilderConfig() {
    }

    public static RequestBuildingService getRequestBuilder() {
        if (instance == null) {
            instance = new RequestBuildingService();
        }
        return instance;
    }
}
