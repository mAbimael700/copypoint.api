package com.copypoint.api.infra.exchangerate.service;

import com.copypoint.api.infra.exchangerate.dto.*;
import com.copypoint.api.infra.exchangerate.http.ExchangeRateClient;
import com.copypoint.api.infra.exchangerate.properties.ExchangeRateProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExchangeRateService {

    @Autowired
    private  ExchangeRateClient exchangeRateClient;

    @Autowired
    private ExchangeRateProperties properties;

    public CurrencyExchangeDto convertCurrency(ConversionRequest request) {
        ConversionResponse response = exchangeRateClient.convertCurrency(
                properties.getApiKey(),
                request.fromCurrency(),
                request.toCurrency(),
                request.amount());

        return new CurrencyExchangeDto(
                response.baseCode(),
                response.targetCode(),
                response.conversionRate(),
                request.amount(),
                response.conversionResult(),
                LocalDateTime.now()
        );
    }

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        return exchangeRateClient
                .getExchangeRate(properties.getApiKey(), fromCurrency, toCurrency)
                .conversionRate();
    }

    public ExchangeRateResponse getAllExchangeRates(String baseCurrency) {
        return exchangeRateClient.getExchangeRates(properties.getApiKey(), baseCurrency);
    }

    public List<CurrencyDto> getSupportedCurrencies() {
        SupportedCodesResponse response = exchangeRateClient.getSupportedCodes(properties.getApiKey());

        return Arrays.stream(response.supportedCodes())
                .map(code -> new CurrencyDto(code[0], code[1]))
                .collect(Collectors.toList());
    }
}
