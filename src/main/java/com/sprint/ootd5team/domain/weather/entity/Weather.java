package com.sprint.ootd5team.domain.weather.entity;


import com.sprint.ootd5team.base.entity.BaseEntity;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "tbl_weathers")
public class Weather extends BaseEntity {

    @Column(name = "forecasted_at", nullable = false)  // 예보 산출 시각
    private Instant forecastedAt;

    @Column(name = "forecast_at", nullable = false) // 예보 대상 시각
    private Instant forecastAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sky_status")
    private SkyStatus SkyStatus;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(name = "x_coord")
    private Integer xCoord;

    @Column(name = "y_coord")
    private Integer yCoord;

    @Column(name = "location_names")
    private String locationNames;

    @Enumerated(EnumType.STRING)
    @Column(name = "precipitation_type")
    private PrecipitationType precipitationType;

    @Column(name = "precipitation_amount")
    private Double precipitationAmount;

    @Column(name = "precipitation_probability")
    private Double precipitationProbability;

    @Column
    private Double humidity;

    @Column(name = "humidity_compared")
    private Double humidityCompared;

    @Column
    private Double temperature;

    @Column(name = "temperature_compared")
    private Double temperatureCompared;

    @Column(name = "temperature_min")
    private Double temperatureMin;

    @Column(name = "temperature_max")
    private Double temperatureMax;

    @Column
    private Double windspeed;

    @Column(name = "windspeed_level")
    private WindspeedLevel windspeedLevel;

//    @ManyToOne(fetch = FetchType.LAZY )
//    @JoinColumn(name = "profile_id", nullable = false)
//    private Profile profile;

}
