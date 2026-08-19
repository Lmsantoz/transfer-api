package com.lucasmarques.transfer_api.service;

import com.lucasmarques.transfer_api.entity.Account;
import com.lucasmarques.transfer_api.entity.Transfer;
import com.lucasmarques.transfer_api.enums.StatusTransfer;
import com.lucasmarques.transfer_api.repository.AccountRepository;
import com.lucasmarques.transfer_api.repository.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;

    public Page<Transfer> findAll(Pageable pageable) {
        return transferRepository.findAll(pageable);
    }

    public void verifyBalance(Account originId, BigDecimal amount) {
        if (originId.getBalance().compareTo(amount) < 0 ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "valor insuficiente");
        }
    }

    private void debit(Account origin, BigDecimal amount) {
        verifyBalance(origin, amount);

        origin.setBalance(origin.getBalance().subtract(amount));
        accountRepository.save(origin);
    }

    private void credit(Account destination, BigDecimal amount) {
        destination.setBalance(destination.getBalance().add(amount));
        accountRepository.save(destination);
    }

    @Transactional
    public Transfer createTransfer(UUID originId, UUID destinationId, BigDecimal amount) {

        if (originId.equals(destinationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "origem e destino devem ser diferentes");
        }

        UUID firstId = originId.compareTo(destinationId) < 0  ? originId : destinationId;
        UUID secondId = firstId.equals(originId) ? destinationId : originId ;

        Account first = accountRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Account second = accountRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Account origin = first.getId().equals(originId) ? first : second;
        Account destination = first.getId().equals(originId) ? second : origin;

        debit(origin, amount);
        credit(destination, amount);

        Transfer transfer = new Transfer(origin, destination, amount, LocalDateTime.now());
        transfer.setStatus(StatusTransfer.SUCCESS);
        transferRepository.save(transfer);
        return transfer;
    }
}