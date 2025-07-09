package com.copypoint.api.infra.exchangerate.config;

import com.copypoint.api.infra.exchangerate.http.ExchangeRateClient;
import com.copypoint.api.infra.exchangerate.properties.ExchangeRateProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ExchangeRateConfig {

    @Bean
    public ExchangeRateClient exchangeRateClient(ExchangeRateProperties properties) {
        RestClient client = RestClient.create(properties.getBaseUrl());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build();
        return factory.createClient(ExchangeRateClient.class);



    }
}
