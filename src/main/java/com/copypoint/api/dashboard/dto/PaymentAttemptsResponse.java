package com.copypoint.api.dashboard.dto;

import java.util.List;

public record PaymentAttemptsResponse(
        List<PaymentAttemptData> attemptsByStatus
) {}
