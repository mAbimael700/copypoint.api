package com.copypoint.api.domain.gatewaycheckout.parser.service;

import com.copypoint.api.domain.gatewaycheckout.dto.CheckoutData;
import com.copypoint.api.domain.gatewaycheckout.parser.dto.ParserStats;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttempt;
import com.copypoint.api.domain.paymentattempt.repository.PaymentAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ParserHealthService {
    private static final Logger logger = LoggerFactory.getLogger(ParserHealthService.class);

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private PaymentAttemptParserService parserService;

    /**
     * Analiza qué parsers se están usando y su efectividad
     */
    public List<ParserStats> getParserStatistics(LocalDateTime since) {
        List<PaymentAttempt> attempts = paymentAttemptRepository
                .findByCreatedAtAfterOrderByCreatedAtDesc(since);

        Map<String, List<PaymentAttempt>> attemptsByParser = new HashMap<>();
        Map<String, List<String>> errorsByParser = new HashMap<>();

        for (PaymentAttempt attempt : attempts) {
            try {
                Optional<CheckoutData> parsed = parserService.parseGatewayResponse(attempt);

                if (parsed.isPresent()) {
                    String version = parsed.get().version();
                    attemptsByParser.computeIfAbsent(version, k -> new ArrayList<>()).add(attempt);
                } else {
                    // Intento determinar qué parser debería haber funcionado
                    String unknownVersion = "unknown-parser";
                    attemptsByParser.computeIfAbsent(unknownVersion, k -> new ArrayList<>()).add(attempt);
                    errorsByParser.computeIfAbsent(unknownVersion, k -> new ArrayList<>())
                            .add("Failed to parse attempt ID: " + attempt.getId());
                }

            } catch (Exception e) {
                errorsByParser.computeIfAbsent("parsing-error", k -> new ArrayList<>())
                        .add("Exception parsing attempt ID: " + attempt.getId() + " - " + e.getMessage());
            }
        }

        return attemptsByParser.entrySet().stream()
                .map(entry -> {
                    String version = entry.getKey();
                    List<PaymentAttempt> versionAttempts = entry.getValue();

                    long total = versionAttempts.size();
                    long successful = versionAttempts.stream()
                            .mapToLong(a -> parserService.parseGatewayResponse(a).isPresent() ? 1 : 0)
                            .sum();
                    long failed = total - successful;
                    double successRate = total > 0 ? (double) successful / total * 100 : 0;

                    LocalDateTime lastUsed = versionAttempts.stream()
                            .map(PaymentAttempt::getCreatedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    List<String> recentErrors = errorsByParser.getOrDefault(version, List.of())
                            .stream()
                            .limit(5) // Solo los 5 errores más recientes
                            .toList();

                    return new ParserStats(version, total, successful, failed, successRate, lastUsed, recentErrors);
                })
                .sorted((a, b) -> Long.compare(b.totalAttempts(), a.totalAttempts()))
                .toList();
    }

    /**
     * Encuentra PaymentAttempts que no se pueden parsear
     */
    public List<PaymentAttempt> findUnparseableAttempts(LocalDateTime since, int limit) {
        List<PaymentAttempt> attempts = paymentAttemptRepository
                .findByCreatedAtAfterOrderByCreatedAtDesc(since)
                .stream()
                .limit(limit)
                .toList();

        return attempts.stream()
                .filter(attempt -> parserService.parseGatewayResponse(attempt).isEmpty())
                .filter(attempt -> attempt.getGatewayResponse() != null) // Tiene respuesta pero no se puede parsear
                .toList();
    }

    /**
     * Verifica la salud general del sistema de parsing
     */
    public Map<String, Object> getOverallHealth(LocalDateTime since) {
        List<ParserStats> stats = getParserStatistics(since);

        long totalAttempts = stats.stream().mapToLong(ParserStats::totalAttempts).sum();
        long totalSuccessful = stats.stream().mapToLong(ParserStats::successfulParses).sum();
        double overallSuccessRate = totalAttempts > 0 ? (double) totalSuccessful / totalAttempts * 100 : 0;

        List<String> availableParsers = parserService.getAvailableParsers();
        List<String> activeParsers = stats.stream()
                .map(ParserStats::parserVersion)
                .filter(v -> !v.equals("unknown-parser"))
                .toList();

        Map<String, Object> health = new HashMap<>();
        health.put("totalAttempts", totalAttempts);
        health.put("successfulParses", totalSuccessful);
        health.put("overallSuccessRate", String.format("%.2f%%", overallSuccessRate));
        health.put("availableParsers", availableParsers);
        health.put("activeParsers", activeParsers);
        health.put("parserStats", stats);
        health.put("healthStatus", overallSuccessRate > 95 ? "HEALTHY" :
                overallSuccessRate > 80 ? "WARNING" : "CRITICAL");

        return health;
    }

    /**
     * Migra datos usando un parser específico (útil para testing)
     */
    public List<CheckoutData> testParserOnHistoricalData(String parserVersion, int limit) {
        List<PaymentAttempt> attempts = paymentAttemptRepository
                .findTop10ByGatewayResponseIsNotNullOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .toList();

        return attempts.stream()
                .map(parserService::parseGatewayResponse)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(data -> data.version().equals(parserVersion))
                .toList();
    }
}
