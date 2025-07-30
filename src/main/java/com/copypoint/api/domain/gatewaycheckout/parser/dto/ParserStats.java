package com.copypoint.api.domain.gatewaycheckout.parser.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ParserStats(
        String parserVersion,
        long totalAttempts,
        long successfulParses,
        long failedParses,
        double successRate,
        LocalDateTime lastUsed,
        List<String> recentErrors
) {
}
