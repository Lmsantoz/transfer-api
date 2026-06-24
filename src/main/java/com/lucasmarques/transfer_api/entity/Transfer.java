package com.lucasmarques.transfer_api.entity;

import com.lucasmarques.transfer_api.enums.StatusTransfer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "transfer")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "origin_id", referencedColumnName = "id")
    private Account originAccount;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "destination_id", referencedColumnName = "id")
    private Account destinationAccount;

    @NotNull
    @Column(nullable = false)
    private BigDecimal amount;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateTransfer;

    @Column
    @Enumerated(EnumType.STRING)
    private StatusTransfer status;
}
