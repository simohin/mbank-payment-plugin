package kz.kaspi.qr.plugin.integration.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import common.config.LogConfig;
import common.config.PluginConfig;
import lombok.Getter;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static java.security.KeyStore.getDefaultType;

public class ClientConfig {
    public static final String PKCS_12 = "PKCS12";
    public static final String X_509 = "X.509";
    private static final String PROTOCOL = "TLS";
    private static ClientConfig INSTANCE;
    public final String baseUrl;
    @Getter
    private final Retrofit retrofit;

    private final Logger logger = LogConfig.getLogger();


    private ClientConfig(String baseUrl, File rootCa, File ca, File cert, String password) {
        this.baseUrl = baseUrl;
        val clientStore = getClientStore(cert, password);
        val keyManagerFactory = getKeyManagerFactory(clientStore, password);
        val keyManagers = keyManagerFactory.getKeyManagers();
        val trustStore = getTrustStore(rootCa, ca);
        val trustManagerFactory = getTrustManagerFactory(trustStore);
        val trustManagers = trustManagerFactory.getTrustManagers();
        val sslContext = getSslContext(keyManagers, trustManagers);
        retrofit = buildRetrofit(sslContext, trustManagers);
    }

    public static void init() {
        val properties = PluginConfig.getProperties();

        val rootCa = Paths.get(properties.getRootCaPath()).toFile();
        val ca = Paths.get(properties.getCaPath()).toFile();
        val cert = Paths.get(properties.getCertPath()).toFile();


        if (Objects.isNull(INSTANCE)) {
            INSTANCE = new ClientConfig(
                    properties.getIp() + "/r2/v01/",
                    rootCa,
                    ca,
                    cert,
                    properties.getCertPassword()
            );
        }
    }

    public static ClientConfig getInstance() {
        if (Objects.isNull(INSTANCE)) {
            throw new IllegalStateException("Not initialized");
        }
        return INSTANCE;
    }

    private static ObjectMapper getObjectMapper() {
        val objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.UpperCamelCaseStrategy.INSTANCE);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    private static TrustManagerFactory getTrustManagerFactory(KeyStore trustStore) {
        TrustManagerFactory trustManagerFactory;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
        return trustManagerFactory;
    }

    private static KeyStore getTrustStore(File rootCa, File ca) {

        KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(getDefaultType());
            trustStore.load(null, null);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance(X_509);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

        loadCerts("ca", ca, certificateFactory, trustStore);
        loadCerts("rootCa", rootCa, certificateFactory, trustStore);
        return trustStore;
    }

    private static void loadCerts(String prefix, File ca, CertificateFactory certificateFactory, KeyStore trustStore) {
        try (InputStream fis = Files.newInputStream(ca.toPath())) {
            AtomicInteger id = new AtomicInteger();
            certificateFactory.generateCertificates(fis).forEach(it -> {
                try {
                    trustStore.setCertificateEntry(prefix + "_" + id.getAndIncrement(), it);
                } catch (KeyStoreException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyManagerFactory getKeyManagerFactory(KeyStore clientStore, String password) {
        KeyManagerFactory keyManagerFactory;
        try {
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientStore, password.toCharArray());
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
        return keyManagerFactory;
    }

    private static KeyStore getClientStore(File cert, String password) {
        KeyStore clientStore;
        try {
            clientStore = KeyStore.getInstance(PKCS_12);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        try (InputStream fis = Files.newInputStream(cert.toPath())) {
            clientStore.load(fis, password.toCharArray());
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return clientStore;
    }

    private SSLContext getSslContext(KeyManager[] keyManagers, TrustManager[] trustManagers) {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return sslContext;
    }

    private Retrofit buildRetrofit(SSLContext sslContext, TrustManager[] trustManagers) {

        val okHttpClient = getHttpClient(sslContext, trustManagers);

        val mapper = getObjectMapper();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();
    }

    private OkHttpClient getHttpClient(SSLContext sslContext, TrustManager[] trustManagers) {

        val x509TrustManager = Arrays.stream(trustManagers)
                .filter(it -> it instanceof X509TrustManager)
                .map(X509TrustManager.class::cast)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not found"));


        val loggingInterceptor = new HttpLoggingInterceptor(logger::debug);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager)
                .addInterceptor(loggingInterceptor)
                .build();
    }
}
