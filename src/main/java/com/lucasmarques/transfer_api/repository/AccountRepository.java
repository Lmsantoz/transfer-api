package com.lucasmarques.transfer_api.repository;

import com.lucasmarques.transfer_api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
