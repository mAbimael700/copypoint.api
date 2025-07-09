package com.copypoint.api.infra.exchangerate.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ExchangeRateProperties{
    @Value("${exchange-rate.base-url}")
    private String baseUrl;

    @Value("${exchange-rate.api-key}")
    private String apiKey;

    @Value("${exchange-rate.timeout:10}")
    private int timeout;
}
