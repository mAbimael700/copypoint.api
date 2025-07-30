package com.copypoint.api.domain.gatewaycheckout;

import com.copypoint.api.domain.gatewaycheckout.dto.CheckoutData;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface GatewayResponseParser {
    Optional<CheckoutData> parse(JsonNode responseNode);
    boolean canParse(JsonNode responseNode);
    String getVersion();
}
