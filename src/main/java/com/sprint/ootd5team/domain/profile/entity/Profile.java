package com.sprint.ootd5team.domain.profile.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "tbl_profiles")
@Entity
public class Profile extends BaseUpdatableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    private String profileImageUrl;

    @Column(name = "latitude", precision = 8, scale = 4)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 8, scale = 4)
    private BigDecimal longitude;

    @Column(name = "x_coord")
    private Integer xCoord;

    @Column(name = "y_coord")
    private Integer yCoord;

    @Column(name = "location_names", length = 100)
    private String locationNames;

    @Column(name = "temperature_sensitivity")
    private Integer temperatureSensitivity;

    @PrePersist
    void prePersist() {
        if (temperatureSensitivity == null) {
            temperatureSensitivity = 2;
        }
    }
}