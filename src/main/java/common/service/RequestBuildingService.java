package common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import common.config.LogConfig;
import common.config.ObjectMapperConfig;
import common.config.PluginConfig;
import common.config.PluginConfigProperties;
import common.exception.BaseError;

import java.nio.charset.StandardCharsets;

public class RequestBuildingService {

    public static final String SLASH = "/";
    private final ObjectMapper mapper = ObjectMapperConfig.getInstance();
    private final Logger log = LogConfig.getLogger();
    private final PluginConfigProperties configProperties = PluginConfig.getProperties();

    public <T> HttpPost buildPost() {
        return buildPost(null, null);
    }
    public <T> HttpPost buildPost(T dto) {
        return buildPost(null, dto);
    }
    public <T> HttpPost buildPost(String path) {
        return buildPost(path, null);
    }

    public <T> HttpPost buildPost(String path, T dto) {

        String ip = configProperties.getIp();
        StringBuilder sb = new StringBuilder(ip);


        if (path != null) {
            if (!ip.endsWith(SLASH) || !path.startsWith(SLASH)) {
                sb.append(SLASH);
            }
            sb.append(path);
        }

        HttpPost httpRequest = new HttpPost(sb.toString());

        if (dto == null) {
            return httpRequest;
        }

        try {
            String payload = mapper.writeValueAsString(dto);
            httpRequest.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
        } catch (Throwable e) {
            throw new BaseError(e);
        }
        return httpRequest;
    }

    public <T> HttpPost buildPostWithDynamicTimeout(T dto) {

        long start = TransactionStartHandler.getTransactionStart();
        long now = System.currentTimeMillis();

        long processDuration = now - start;
        long socketTimeout = processDuration > 0 ? processDuration : 0;

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(configProperties.getConnectionTimeout())
                .setSocketTimeout((int) socketTimeout)
                .build();

        HttpPost httpRequest = new HttpPost(configProperties.getIp());
        httpRequest.setConfig(requestConfig);

        try {
            String payload = mapper.writeValueAsString(dto);
            httpRequest.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
        } catch (Throwable e) {
            throw new BaseError(e);
        }
        return httpRequest;
    }
}
