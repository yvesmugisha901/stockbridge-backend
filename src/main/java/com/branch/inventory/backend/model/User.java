package com.branch.inventory.backend.model;

import com.branch.inventory.backend.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * User entity — represents every system user.
 *
 * FR-01: name, email, password, role, branch assignment
 * FR-04: passwordHash stored as bcrypt
 * FR-05: active flag — deactivated users cannot log in
 * NFR-02: passwords hashed with bcrypt cost >= 12
 *
 * Implements UserDetails so Spring Security can use it directly.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Branch assignment — null only for ADMIN and HO_ADMIN. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * FR-REG: Self-registered users start inactive and pending approval.
     * An ADMIN must approve them before they can log in.
     * Set to false once approved (or for users created directly by an admin).
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean pendingApproval = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── UserDetails implementation ────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Spring Security uses email as the username. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Locked if deactivated OR still pending admin approval.
     * Spring Security returns 401 for locked accounts automatically.
     */
    @Override
    public boolean isAccountNonLocked() {
        return active && !pendingApproval;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
