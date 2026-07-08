package com.lucasmarques.transfer_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Data
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String cpf;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;

    public Client(String name, String number, String mail) {
        this.name = name;
        this.cpf = number;
        this.email = mail;
    }

    public Client() {}
}
