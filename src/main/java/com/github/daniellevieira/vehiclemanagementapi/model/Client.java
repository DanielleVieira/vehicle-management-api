package com.github.daniellevieira.vehiclemanagementapi.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Entity
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 255)
    private String email;
    @Column(nullable = false, length = 11, unique = true)
    private String cpf;
    @Column(nullable = false)
    private LocalDate birthDate;

    protected Client() { // protected para o mapstruct preferir usar o construtos com argumentos. jpa continua funcionando.
    }

    public Client(String name, String email, String cpf, LocalDate birthDate) {
        this.name = name.trim().toUpperCase();
        this.email = email.trim().toLowerCase();
        this.cpf = cpf.trim();
        this.birthDate = birthDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", cpf=" + cpf +
                ", birthDate=" + birthDate +
                '}';
    }
}

