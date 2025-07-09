package com.copypoint.api.infra.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupportedCodesResponse(
        @JsonProperty("result") String result,
        @JsonProperty("documentation") String documentation,
        @JsonProperty("terms_of_use") String termsOfUse,
        @JsonProperty("supported_codes") String[][] supportedCodes
) {
}
