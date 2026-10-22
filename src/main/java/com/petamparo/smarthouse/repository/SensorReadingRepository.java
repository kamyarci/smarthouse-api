package com.petamparo.smarthouse.repository;

import com.petamparo.smarthouse.entity.SensorReadingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReadingEntity, Long> {
}
