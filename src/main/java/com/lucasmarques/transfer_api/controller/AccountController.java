package com.lucasmarques.transfer_api.controller;

import com.lucasmarques.transfer_api.entity.Account;
import com.lucasmarques.transfer_api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Account getTransfer(@PathVariable UUID id) {
        return accountService.findById(id);
    }

}
