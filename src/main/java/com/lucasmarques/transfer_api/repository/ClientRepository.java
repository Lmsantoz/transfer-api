package com.lucasmarques.transfer_api.repository;

import com.lucasmarques.transfer_api.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
