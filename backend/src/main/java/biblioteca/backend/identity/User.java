package biblioteca.backend.identity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String name;
    @Column(name = "password_hash")
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean enabled = true;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected User() {
    }

    User(String username, String name, String passwordHash, Role role) {
        this.username = username;
        this.name = name;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    void update(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    Long getId() {
        return id;
    }

    String getUsername() {
        return username;
    }

    String getName() {
        return name;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    Role getRole() {
        return role;
    }

    boolean isEnabled() {
        return enabled;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
