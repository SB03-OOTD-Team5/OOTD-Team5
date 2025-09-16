package com.sprint.ootd5team.domain.weather.repository;

import com.sprint.ootd5team.domain.weather.entity.Weather;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {

}
