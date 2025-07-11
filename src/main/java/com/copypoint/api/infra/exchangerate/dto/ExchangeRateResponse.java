package com.copypoint.api.infra.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateResponse(
        @JsonProperty("result") String result,
        @JsonProperty("documentation") String documentation,
        @JsonProperty("terms_of_use") String termsOfUse,
        @JsonProperty("time_last_update_unix") Long timeLastUpdateUnix,
        @JsonProperty("time_last_update_utc") String timeLastUpdateUtc,
        @JsonProperty("time_next_update_unix") Long timeNextUpdateUnix,
        @JsonProperty("time_next_update_utc") String timeNextUpdateUtc,
        @JsonProperty("base_code") String baseCode,
        @JsonProperty("conversion_rates") Map<String, BigDecimal> conversionRates
) {
}
