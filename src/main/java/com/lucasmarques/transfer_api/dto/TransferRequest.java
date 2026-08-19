package com.lucasmarques.transfer_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(@NotNull UUID originId, @NotNull UUID destinationId, @NotNull @Positive BigDecimal amount) {
}
