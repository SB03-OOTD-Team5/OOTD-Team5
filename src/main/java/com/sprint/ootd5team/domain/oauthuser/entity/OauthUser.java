package com.sprint.ootd5team.domain.oauthuser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_oauth_users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OauthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private String id;

    @Column(name = "user_id",nullable = false)
    private UUID userId;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;


    public OauthUser(UUID userId, String provider, String providerId) {
        this.userId = userId;
        this.provider = provider;
        this.providerId = providerId;
    }

    @Override
    public String toString() {
        return "OauthUser{" +
            "id='" + id + '\'' +
            ", userId=" + userId +
            ", provider='" + provider + '\'' +
            ", providerId='" + providerId + '\'' +
            '}';
    }
}
