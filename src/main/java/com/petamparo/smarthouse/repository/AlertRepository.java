package com.petamparo.smarthouse.repository;

import com.petamparo.smarthouse.entity.AlertEntity;
import com.petamparo.smarthouse.enums.AlertStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<AlertEntity, Long>, JpaSpecificationExecutor<AlertEntity> {

    List<AlertEntity> findByDeviceIdAndStatusInOrderByCreatedAtDesc(String deviceId, Collection<AlertStatus> statuses);

    List<AlertEntity> findByStatusInOrderByCreatedAtDesc(Collection<AlertStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AlertEntity a where a.deviceId = :deviceId and a.status in :statuses")
    List<AlertEntity> findForUpdate(@Param("deviceId") String deviceId,
                                    @Param("statuses") Collection<AlertStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AlertEntity a where a.id = :id")
    Optional<AlertEntity> findByIdForUpdate(@Param("id") Long id);
}
