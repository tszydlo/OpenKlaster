package com.openklaster.app.persistence.cassandra.dao;

import com.openklaster.app.model.entities.measurement.LoadMeasurementEntity;
import com.openklaster.app.model.entities.measurement.SourceMeasurementEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SourceMeasurementRepository extends CassandraRepository<SourceMeasurementEntity, String> {

    List<SourceMeasurementEntity> findByTimestampBetweenAndUnitAndInstallationId(Date startDate, Date endDate, String unit,
                                                                               String installationId);
    List<SourceMeasurementEntity> findByTimestampBeforeAndUnitAndInstallationId(Date endDate, String unit,
                                                                                 String installationId);
    List<SourceMeasurementEntity> findByTimestampAfterAndUnitAndInstallationId(Date startDate, String unit,
                                                                                 String installationId);
    List<SourceMeasurementEntity> findByInstallationIdAndUnit(String installationId, String unit);
}
