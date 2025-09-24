package com.sprint.ootd5team.domain.profile.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
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

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void update(String name, String gender, LocalDate birthDate, WeatherAPILocationDto location, Integer temperatureSensitivity){
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.temperatureSensitivity = temperatureSensitivity;

        if(location != null) {
            this.latitude = location.latitude();
            this.longitude = location.longitude();
            this.xCoord = location.x();
            this.yCoord = location.y();
            this.locationNames = String.join(" ", location.locationNames());
        }
    }
    public void relocate(BigDecimal latitude, BigDecimal longitude, String locationNames) {
        if (!Objects.equals(this.latitude, latitude) && latitude != null) {
            this.latitude = latitude;
        }

        if (!Objects.equals(this.longitude, longitude) && longitude != null) {
            this.longitude = longitude;
        }

        if (!Objects.equals(this.locationNames, locationNames) && locationNames != null) {
            this.locationNames = locationNames;
        }
    }
}