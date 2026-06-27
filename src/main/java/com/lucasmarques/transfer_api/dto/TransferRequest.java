package com.lucasmarques.transfer_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(UUID originId, UUID destinationId, BigDecimal amount) {
}
