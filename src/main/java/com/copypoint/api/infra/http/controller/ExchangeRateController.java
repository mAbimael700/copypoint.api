package com.copypoint.api.infra.http.controller;

import com.copypoint.api.infra.exchangerate.dto.ConversionRequest;
import com.copypoint.api.infra.exchangerate.dto.CurrencyDto;
import com.copypoint.api.infra.exchangerate.dto.CurrencyExchangeDto;
import com.copypoint.api.infra.exchangerate.dto.ExchangeRateResponse;
import com.copypoint.api.infra.exchangerate.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/exchange-rate")
public class ExchangeRateController {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @PostMapping("/convert")
    public ResponseEntity<CurrencyExchangeDto> convertCurrency(@RequestBody ConversionRequest request) {
        return ResponseEntity.ok(exchangeRateService.convertCurrency(request));

    }

    @GetMapping("/rate/{from}/{to}")
    public ResponseEntity<BigDecimal> getExchangeRate(
            @PathVariable String from,
            @PathVariable String to) {
        return ResponseEntity.ok(exchangeRateService.getExchangeRate(from, to));
    }

    @GetMapping("/rates/{baseCurrency}")
    public ResponseEntity<ExchangeRateResponse> getAllRates(@PathVariable String baseCurrency) {
        return ResponseEntity.ok(exchangeRateService.getAllExchangeRates(baseCurrency));
    }

    @GetMapping("/currencies")
    public ResponseEntity<List<CurrencyDto>> getSupportedCurrencies() {
        return ResponseEntity.ok(exchangeRateService.getSupportedCurrencies());
    }
}
