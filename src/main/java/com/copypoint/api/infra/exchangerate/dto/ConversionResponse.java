package com.copypoint.api.infra.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ConversionResponse(
        @JsonProperty("result") String result,
        @JsonProperty("documentation") String documentation,
        @JsonProperty("terms_of_use") String termsOfUse,
        @JsonProperty("base_code") String baseCode,
        @JsonProperty("target_code") String targetCode,
        @JsonProperty("conversion_rate") BigDecimal conversionRate,
        @JsonProperty("conversion_result") BigDecimal conversionResult
) {
}
