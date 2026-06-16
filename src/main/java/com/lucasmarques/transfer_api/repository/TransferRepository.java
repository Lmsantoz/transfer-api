package com.lucasmarques.transfer_api.repository;

import com.lucasmarques.transfer_api.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
}
