package com.sprint.ootd5team.domain.user.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_users")
@NoArgsConstructor
@Getter
public class User extends BaseUpdatableEntity {

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name="password",length = 100, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private Role role;

    @Column(name="is_locked",nullable = false)
    private Boolean locked;

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.locked = false;
    }

    public void updatePassword(String newPassword){
        this.password = newPassword;
    }

    public void resetPassword(){
        this.password = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
    }

    public void updateRole(Role role){
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
            "name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", password='" + password + '\'' +
            ", role=" + role +
            ", is_locked=" + locked +
            "} " + super.toString();
    }
}
