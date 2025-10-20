package com.sprint.ootd5team.domain.profile.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "tbl_profiles")
@Entity
public class Profile extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "temperature_sensitivity")
    private Integer temperatureSensitivity;

    @PrePersist
    void prePersist() {
        if (temperatureSensitivity == null) {
            temperatureSensitivity = 2;
        }
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void update(String name, String gender, LocalDate birthDate, Location location, Integer temperatureSensitivity){
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.temperatureSensitivity = temperatureSensitivity;
        relocate(location);
    }
    public void relocate(Location location) {
        this.location = location;
    }
}