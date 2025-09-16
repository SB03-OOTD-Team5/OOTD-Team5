package com.sprint.ootd5team.domain.user.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
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

    @Column(name="password",length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private Role role;

    @Column(name="is_locked",nullable = false)
    private Boolean locked;

    // 임시 비밀번호
    @Column(name="temp_password",length = 100)
    private String tempPassword;

    //임시 비밀번호 만료기한
    @Column(name="temp_password_expired_at")
    private Instant tempPasswordExpireAt;

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.locked = false;
    }

    public void updatePassword(String newPassword){
        this.password = newPassword;
        // 임시 비밀번호 삭제
        this.tempPassword = null;
        this.tempPasswordExpireAt = null;
    }

    public void issueTemporaryPassword(){
        // 랜덤 임시 비밀번호 발급
        this.tempPassword = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        // 3분후에 만료
        this.tempPasswordExpireAt = Instant.now().plusSeconds(3 * 60);
    }

    /**
     * 현재 임시 비밀번호가 유효한지 확인하는 메서드
     * @param input 확인하
     * @return 유효 여부
     */
    public boolean validateTemporaryPassword(String input) {
        return tempPassword != null
            && tempPassword.equals(input)
            && tempPasswordExpireAt != null
            && Instant.now().isBefore(tempPasswordExpireAt);
    }

    /**
     * 임시 비밀번호 초기화 메서드
     */
    public void clearTemporaryPassword() {
        this.tempPassword = null;
        this.tempPasswordExpireAt = null;
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
