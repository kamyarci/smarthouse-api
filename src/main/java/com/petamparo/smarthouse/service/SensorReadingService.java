package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.dto.CreateSensorReadingRequestDTO;
import com.petamparo.smarthouse.entity.SensorReadingEntity;
import com.petamparo.smarthouse.repository.SensorReadingRepository;
import org.springframework.stereotype.Service;

@Service
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;

    public SensorReadingService(SensorReadingRepository sensorReadingRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
    }

    public SensorReadingEntity create(CreateSensorReadingRequestDTO request) {
        SensorReadingEntity sensorReading = new SensorReadingEntity();

        sensorReading.setTemperature(request.temperature());
        sensorReading.setHumidity(request.humidity());
        sensorReading.setMotionDetected(request.motionDetected());
        sensorReading.setFoodWeightKg(request.foodWeightKg());
        sensorReading.setFoodPercentage(request.foodPercentage());
        sensorReading.setWaterWeightKg(request.waterWeightKg());
        sensorReading.setWaterPercentage(request.waterPercentage());

        return sensorReadingRepository.save(sensorReading);
    }
}
