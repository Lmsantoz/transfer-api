package com.lucasmarques.transfer_api;

import com.lucasmarques.transfer_api.entity.Account;
import com.lucasmarques.transfer_api.entity.Client;
import com.lucasmarques.transfer_api.repository.AccountRepository;
import com.lucasmarques.transfer_api.repository.ClientRepository;
import com.lucasmarques.transfer_api.service.TransferService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

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


    private Client client;
    private Account account;
    private Client client2;
    private Account account2;

    @BeforeEach
    void beforeEach() {
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
    void Transfer() {
        assertThat(accountRepository.findById(account.getId())).isPresent();
        assertThat(accountRepository.findById(account2.getId())).isPresent();

        transferService.createTransfer(account.getId(), account2.getId(), BigDecimal.valueOf(200.00));

        Assertions.assertEquals(0,accountRepository.findById(account.getId()).get().getBalance().compareTo(BigDecimal.valueOf(0.0)));
        Assertions.assertEquals(0, accountRepository.findById(account2.getId()).get().getBalance().compareTo(BigDecimal.valueOf(500.00)));
    }
}
