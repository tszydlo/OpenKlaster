package com.openklaster.app.services;

import com.openklaster.app.model.entities.measurement.LoadMeasurementEntity;
import com.openklaster.app.model.entities.measurement.SourceMeasurementEntity;
import com.openklaster.app.model.requests.MeasurementRequest;
import com.openklaster.app.model.responses.MeasurementResponse;
import com.openklaster.app.persistence.cassandra.dao.LoadMeasurementRepository;
import com.openklaster.app.persistence.cassandra.dao.SourceMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MeasurementService {
    @Autowired
    LoadMeasurementRepository loadMeasurementRepository;
    @Autowired
    SourceMeasurementRepository sourceMeasurementRepository;

    public MeasurementResponse addLoadMeasurement(MeasurementRequest request, String unit) {
        Date date = Optional.ofNullable(request.getTimestamp()).orElse(new Date());
        LoadMeasurementEntity newMeasurement = LoadMeasurementEntity.builder()
                .installationId(request.getInstallationId())
                .unit(unit)
                .value(request.getValue())
                .timestamp(date)
                .build();
        loadMeasurementRepository.save(newMeasurement);
        return createLoadMeasurementResponse(request, unit, date);
    }

    public MeasurementResponse addSourceMeasurement(MeasurementRequest request, String unit) {
        Date date = Optional.ofNullable(request.getTimestamp()).orElse(new Date());
        SourceMeasurementEntity newMeasurement = SourceMeasurementEntity.builder()
                .installationId(request.getInstallationId())
                .unit(unit)
                .value(request.getValue())
                .timestamp(date)
                .build();
        sourceMeasurementRepository.save(newMeasurement);
        return createLoadMeasurementResponse(request, unit, date);
    }

    public List<MeasurementResponse> getLoadMeasurements(String installationId, Date startDate, Date endDate, String unit) {
        Optional<Date> startDateOpt = Optional.ofNullable(startDate);
        Optional<Date> endDateOpt = Optional.ofNullable(endDate);
        return getLoadEntities(installationId, startDateOpt, endDateOpt, unit)
                .stream()
                .map(this::loadMeasurementEntityToResponse)
                .limit(1000)
                .collect(Collectors.toList());

    }

    private List<LoadMeasurementEntity> getLoadEntities(String installationId, Optional<Date> startDateOpt,
                                                        Optional<Date> endDateOpt, String unit) {
        if (startDateOpt.isPresent() && endDateOpt.isPresent()) {
            return loadMeasurementRepository.findByTimestampBetweenAndUnitAndInstallationId(startDateOpt.get(),
                    endDateOpt.get(), unit, installationId);
        } else if (startDateOpt.isPresent()) {
            return loadMeasurementRepository.findByTimestampAfterAndUnitAndInstallationId(startDateOpt.get(),
                    unit, installationId);
        } else if (endDateOpt.isPresent()) {
            return loadMeasurementRepository.findByTimestampBeforeAndUnitAndInstallationId(endDateOpt.get(),
                    unit, installationId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one date should be provided");
        }
    }

    public List<MeasurementResponse> getSourceMeasurements(String installationId, Date startDate, Date endDate, String unit) {
        Optional<Date> startDateOpt = Optional.ofNullable(startDate);
        Optional<Date> endDateOpt = Optional.ofNullable(endDate);
        return getSourceEntities(installationId, startDateOpt, endDateOpt, unit)
                .stream()
                .map(this::sourceMeasurementEntityToResponse)
                .limit(1000)
                .collect(Collectors.toList());

    }

    private List<SourceMeasurementEntity> getSourceEntities(String installationId, Optional<Date> startDateOpt,
                                                        Optional<Date> endDateOpt, String unit) {
        if (startDateOpt.isPresent() && endDateOpt.isPresent()) {
            return sourceMeasurementRepository.findByTimestampBetweenAndUnitAndInstallationId(startDateOpt.get(),
                    endDateOpt.get(), unit, installationId);
        } else if (startDateOpt.isPresent()) {
            return sourceMeasurementRepository.findByTimestampAfterAndUnitAndInstallationId(startDateOpt.get(),
                    unit, installationId);
        } else if (endDateOpt.isPresent()) {
            return sourceMeasurementRepository.findByTimestampBeforeAndUnitAndInstallationId(endDateOpt.get(),
                    unit, installationId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one date should be provided");
        }
    }

    private MeasurementResponse createLoadMeasurementResponse(MeasurementRequest request, String unit, Date date) {
        return MeasurementResponse.builder()
                .installationId(request.getInstallationId())
                .timestamp(date)
                .unit(unit)
                .value(request.getValue())
                .build();
    }

    private MeasurementResponse loadMeasurementEntityToResponse(LoadMeasurementEntity entity) {
        return MeasurementResponse.builder()
                .installationId(entity.getInstallationId())
                .unit(entity.getUnit())
                .timestamp(entity.getTimestamp())
                .value(entity.getValue())
                .build();
    }

    private MeasurementResponse sourceMeasurementEntityToResponse(SourceMeasurementEntity entity) {
        return MeasurementResponse.builder()
                .installationId(entity.getInstallationId())
                .unit(entity.getUnit())
                .timestamp(entity.getTimestamp())
                .value(entity.getValue())
                .build();
    }
}
