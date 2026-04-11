package com.github.daniellevieira.vehiclemanagementapi.model.auth;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false,  length = 255)
    private String email;
    @Column(nullable = false,   length = 255)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    protected User() {}

    public User(String email, String password) {
        setProperties(this, this.id, email, password, Role.USER);
    }

    private User setProperties(User newUser, Long id, String newEmail, String newPassword, Role newRole) {
        newUser.id = id;
        newUser.email = Objects.requireNonNull(newEmail).trim().toLowerCase();
        newUser.password = Objects.requireNonNull(newPassword).trim();
        newUser.role = Objects.requireNonNull(newRole);
        return newUser;
    }

    public User updateUser(String newEmail, String newPassword, Role newRole) {
        return setProperties(new User(), this.id, newEmail, newPassword, newRole);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != 0 && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
