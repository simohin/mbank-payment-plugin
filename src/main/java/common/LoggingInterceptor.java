package common;

import common.config.LogConfig;
import org.apache.http.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LoggingInterceptor implements HttpResponseInterceptor, HttpRequestInterceptor {

    private static final Logger LOGGER = LogConfig.getLogger();


    @Override
    public void process(HttpRequest request, HttpContext context) throws IOException {
        String message = buildRequestEntry(request, context)
                + buildHeadersEntry(request.getAllHeaders())
                + buildEntityEntry(request);
        LOGGER.info(message);
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {
        String message = buildStatusEntry(response)
                + buildHeadersEntry(response.getAllHeaders())
                + buildEntityEntry(response);
        LOGGER.info(message);
    }

    private String buildStatusEntry(HttpResponse response) {
        return "\nResponse - "
                + response.getStatusLine().getStatusCode() + " "
                + response.getStatusLine().getReasonPhrase();
    }

    private String buildRequestEntry(HttpRequest request, HttpContext context) {
        return "\nRequest - "
                + request.getRequestLine().getMethod() + " "
                + context.getAttribute("http.target_host")
                + request.getRequestLine().getUri();
    }

    private String buildHeadersEntry(Header[] headers) {
        return "\nHeaders: ["
                + Arrays.stream(headers)
                .map(header -> header.getName() + ": " + header.getValue())
                .collect(Collectors.joining(", "))
                + "]";
    }

    private String buildEntityEntry(HttpRequest request) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            entity.writeTo(bs);
        }
        return "\nPayload:\n" + bs;
    }

    private String buildEntityEntry(HttpResponse response) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String payload = buffer.lines().collect(Collectors.joining("\n"));
        response.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
        return "\nPayload: \n" + payload;
    }
}
