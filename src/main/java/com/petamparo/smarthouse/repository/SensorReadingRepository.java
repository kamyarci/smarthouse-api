package com.petamparo.smarthouse.repository;

import com.petamparo.smarthouse.entity.SensorReadingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SensorReadingRepository extends JpaRepository<SensorReadingEntity, Long>,
        JpaSpecificationExecutor<SensorReadingEntity> {

    Optional<SensorReadingEntity> findFirstByDeviceIdOrderByRecordedAtDescIdDesc(String deviceId);
}
