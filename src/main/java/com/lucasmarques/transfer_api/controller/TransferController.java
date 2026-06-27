package com.lucasmarques.transfer_api.controller;

import com.lucasmarques.transfer_api.dto.TransferRequest;
import com.lucasmarques.transfer_api.entity.Account;
import com.lucasmarques.transfer_api.entity.Transfer;
import com.lucasmarques.transfer_api.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer create(@RequestBody TransferRequest transferRequest) {
        return transferService.createTransfer(transferRequest.originId(), transferRequest.destinationId(), transferRequest.amount());
    }


}
