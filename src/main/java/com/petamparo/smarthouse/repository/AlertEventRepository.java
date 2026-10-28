package com.petamparo.smarthouse.repository;

import com.petamparo.smarthouse.entity.AlertEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertEventRepository extends JpaRepository<AlertEventEntity, Long> {

    List<AlertEventEntity> findByAlertIdOrderByCreatedAtAscIdAsc(Long alertId);
}
