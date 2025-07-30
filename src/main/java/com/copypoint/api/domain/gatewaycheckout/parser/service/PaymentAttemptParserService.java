package com.copypoint.api.domain.gatewaycheckout.parser.service;

import com.copypoint.api.domain.gatewaycheckout.GatewayResponseParser;
import com.copypoint.api.domain.gatewaycheckout.dto.CheckoutData;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttempt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentAttemptParserService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentAttemptParserService.class);
    private final ObjectMapper objectMapper;
    private final List<GatewayResponseParser> parsers;

    public PaymentAttemptParserService(ObjectMapper objectMapper, List<GatewayResponseParser> parsers) {
        this.objectMapper = objectMapper;
        this.parsers = parsers;
        logger.info("Initialized with {} parsers: {}", parsers.size(),
                parsers.stream().map(GatewayResponseParser::getVersion).toList());
    }

    /**
     * Parsea la respuesta del gateway usando el parser apropiado
     */
    public Optional<CheckoutData> parseGatewayResponse(PaymentAttempt paymentAttempt) {
        if (paymentAttempt.getGatewayResponse() == null) {
            return Optional.empty();
        }

        try {
            JsonNode responseNode = objectMapper.readTree(paymentAttempt.getGatewayResponse());

            // Buscar el parser apropiado
            for (GatewayResponseParser parser : parsers) {
                if (parser.canParse(responseNode)) {
                    logger.debug("Using parser {} for PaymentAttempt ID: {}",
                            parser.getVersion(), paymentAttempt.getId());
                    return parser.parse(responseNode);
                }
            }

            logger.warn("No parser found for PaymentAttempt ID: {}, JSON structure: {}",
                    paymentAttempt.getId(), responseNode.toPrettyString());
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Error parsing gateway response for PaymentAttempt ID: {}",
                    paymentAttempt.getId(), e);
            return Optional.empty();
        }
    }

    /**
     * Obtiene información sobre qué parser se usó
     */
    public Optional<String> getParserVersion(PaymentAttempt paymentAttempt) {
        return parseGatewayResponse(paymentAttempt)
                .map(CheckoutData::version);
    }

    /**
     * Lista todos los parsers disponibles (útil para debugging)
     */
    public List<String> getAvailableParsers() {
        return parsers.stream()
                .map(GatewayResponseParser::getVersion)
                .toList();
    }

    /**
     * Obtiene solo la URL de checkout (backwards compatible)
     */
    public Optional<String> getCheckoutUrl(PaymentAttempt paymentAttempt) {
        return parseGatewayResponse(paymentAttempt)
                .map(CheckoutData::checkoutUrl);
    }

    /**
     * Obtiene la URL de sandbox para testing (backwards compatible)
     */
    public Optional<String> getSandboxCheckoutUrl(PaymentAttempt paymentAttempt) {
        return parseGatewayResponse(paymentAttempt)
                .map(CheckoutData::sandboxUrl);
    }
}
