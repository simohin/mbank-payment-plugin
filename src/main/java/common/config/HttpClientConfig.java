package common.config;

import common.LoggingInterceptor;
import org.apache.http.Header;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.util.HashSet;

public class HttpClientConfig {

    private static final PluginConfigProperties configProperties = PluginConfig.getProperties();

    private static final LoggingInterceptor loggingInterceptor = new LoggingInterceptor();

    private HttpClientConfig() {
    }

    public static HttpClientBuilder getBuilder() {
        RequestConfig config = getRequestConfig();

        HashSet<Header> headers = new HashSet<>();
        headers.add(new BasicHeader("Content-Type", "application/json; charset=utf-8"));
        headers.add(new BasicHeader("Accept", "application/json; charset=utf-8"));

        return HttpClientBuilder.create()
                .addInterceptorFirst((HttpRequestInterceptor) loggingInterceptor)
                .addInterceptorLast((HttpResponseInterceptor) loggingInterceptor)
                .setDefaultHeaders(headers)
                .setDefaultRequestConfig(config)
                .disableAutomaticRetries()
                .setServiceUnavailableRetryStrategy(getServiceUnavailableStrategy());
    }

    private static ServiceUnavailableRetryStrategy getServiceUnavailableStrategy() {
        return new ServiceUnavailableRetryStrategy() {

            @Override
            public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
                return false;
            }

            @Override
            public long getRetryInterval() {
                return 0;
            }
        };
    }

    private static RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(configProperties.getConnectionTimeout())
                .setSocketTimeout(configProperties.getReadTimeout())
                .build();
    }
}
