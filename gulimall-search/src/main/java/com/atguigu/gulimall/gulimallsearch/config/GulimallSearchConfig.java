package com.atguigu.gulimall.gulimallsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GulimallSearchConfig {

    @Value("${elasticSearch.hostname}")
    private String hostname;

    @Value("${elasticSearch.port}")
    private int port;

    @Value("${elasticSearch.scheme}")
    private String scheme;

    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient client() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, port, scheme));
        return new RestHighLevelClient(builder);
    }
}
