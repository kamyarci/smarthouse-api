package com.petamparo.smarthouse.repository;

import com.petamparo.smarthouse.entity.SystemSettingsEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SystemSettingsRepository extends JpaRepository<SystemSettingsEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SystemSettingsEntity s where s.id = :id")
    Optional<SystemSettingsEntity> findByIdForUpdate(@Param("id") Integer id);
}
