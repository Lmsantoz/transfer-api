package com.lucasmarques.transfer_api;

import com.lucasmarques.transfer_api.entity.Account;
import com.lucasmarques.transfer_api.entity.Client;
import com.lucasmarques.transfer_api.repository.AccountRepository;
import com.lucasmarques.transfer_api.repository.ClientRepository;

import com.lucasmarques.transfer_api.repository.TransferRepository;

import com.lucasmarques.transfer_api.service.TransferService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class TransferServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testConnection() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;


    @Autowired
    private TransferRepository transferRepository;



    private Client client;
    private Account account;
    private Client client2;
    private Account account2;

    @BeforeEach
    void beforeEach() {

        transferRepository.deleteAll();

        accountRepository.deleteAll();
        clientRepository.deleteAll();

        client = clientRepository.save(new Client("Lucas", "12345678901", "lucas@gmail.com"));
        client2 = clientRepository.save(new Client("Pedro", "12345678902", "pedro@gmail.com"));

        account = accountRepository.save(new Account(BigDecimal.valueOf(200.00), "12345", client));
        account2 = accountRepository.save(new Account(BigDecimal.valueOf(300.00), "12346", client2));
    }

    @Autowired
    private TransferService transferService;

    @Test
    void TransferSuccess() {
        assertThat(accountRepository.findById(account.getId())).isPresent();
        assertThat(accountRepository.findById(account2.getId())).isPresent();

        transferService.createTransfer(account.getId(), account2.getId(), BigDecimal.valueOf(200.00));

        Assertions.assertEquals(0,accountRepository.findById(account.getId()).get().getBalance().compareTo(BigDecimal.valueOf(0.0)));
        Assertions.assertEquals(0, accountRepository.findById(account2.getId()).get().getBalance().compareTo(BigDecimal.valueOf(500.00)));
    }

    @Test
    void TransferFail() {
        assertThat(accountRepository.findById(account.getId())).isPresent();
        assertThat(accountRepository.findById(account2.getId())).isPresent();

        Assertions.assertThrows(ResponseStatusException.class, () -> transferService.createTransfer(account.getId(), account2.getId(), BigDecimal.valueOf(400.00)));
    }


    @Test
    void TransferConcurrent() throws InterruptedException {
        int threads = 10;
        BigDecimal amount = BigDecimal.valueOf(50.0);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0;i < threads; i++) {
            executor.submit(()-> {
                try {
                    transferService.createTransfer(account.getId(), account2.getId(), amount);
                    successCount.incrementAndGet();
                } catch (Exception e){

                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        BigDecimal originBalance = accountRepository.findById(account.getId()).get().getBalance();
        BigDecimal destBalance = accountRepository.findById(account2.getId()).get().getBalance();

        Assertions.assertEquals(4, successCount.get());
        Assertions.assertEquals(0, originBalance.compareTo(BigDecimal.ZERO));
        Assertions.assertEquals(0, destBalance.compareTo(BigDecimal.valueOf(500.00)));
    }
}
