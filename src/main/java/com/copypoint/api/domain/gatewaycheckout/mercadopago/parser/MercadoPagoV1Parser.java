package com.copypoint.api.domain.gatewaycheckout.mercadopago.parser;

import com.copypoint.api.domain.gatewaycheckout.GatewayResponseParser;
import com.copypoint.api.domain.gatewaycheckout.dto.CheckoutData;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class MercadoPagoV1Parser implements GatewayResponseParser {
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoV1Parser.class);

    @Override
    public boolean canParse(JsonNode responseNode) {
        // Detecta estructura v1: tiene initPoint, sandboxInitPoint y type
        return responseNode.has("initPoint") &&
                responseNode.has("sandboxInitPoint") &&
                responseNode.has("type") &&
                responseNode.has("id");
    }

    @Override
    public Optional<CheckoutData> parse(JsonNode responseNode) {
        try {
            String initPoint = responseNode.path("initPoint").asText();
            String sandboxInitPoint = responseNode.path("sandboxInitPoint").asText();
            String id = responseNode.path("id").asText();
            String externalReference = responseNode.path("externalReference").asText();
            String type = responseNode.path("type").asText();

            LocalDateTime dateCreated = null;
            if (responseNode.has("dateCreated")) {
                String dateStr = responseNode.path("dateCreated").asText();
                dateCreated = parseISODateTime(dateStr);
            }

            return Optional.of(new CheckoutData(
                    initPoint,
                    id,
                    externalReference,
                    sandboxInitPoint,
                    type,
                    dateCreated,
                    getVersion()
            ));

        } catch (Exception e) {
            logger.error("Error parsing MercadoPago v1 response", e);
            return Optional.empty();
        }
    }

    @Override
    public String getVersion() {
        return "mercadopago-v1";
    }

    private LocalDateTime parseISODateTime(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr.substring(0, 19));
        } catch (Exception e) {
            logger.warn("Error parsing date: {}", dateStr);
            return null;
        }
    }
}
