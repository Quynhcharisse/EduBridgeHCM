package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "account")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Account implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String email;

    @Enumerated(EnumType.STRING)
    Role role;

    @Column(name = "register_date")
    LocalDate registerDate;

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "first_login")
    Boolean firstLogin;

    @Column(name = "is_restricted")
    boolean isRestricted;

    @Column(name = "restriction_reason")
    String restrictionReason;

    @Column(name = "restriction_date")
    LocalDateTime restrictionDate;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Counsellor counsellor;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Parent parent;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Campus campus;

    @OneToMany(mappedBy = "account")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Post> postList;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setIsRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }
}
