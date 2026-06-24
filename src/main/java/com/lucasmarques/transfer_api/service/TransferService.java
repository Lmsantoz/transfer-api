package com.lucasmarques.transfer_api.service;

import com.lucasmarques.transfer_api.entity.Account;
import com.lucasmarques.transfer_api.entity.Transfer;
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

    private Account findOriginById(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Account findDestinationById(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
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
    public void createTransfer(UUID originId, UUID destinationId, BigDecimal amount) {

        Account origin = findOriginById(originId);
        Account destination =  findDestinationById(destinationId);

        debit(origin, amount);
        credit(destination, amount);
        LocalDateTime date = LocalDateTime.now();

        Transfer transfer = new Transfer(origin, destination, amount, date);
        transferRepository.save(transfer);
    }
}