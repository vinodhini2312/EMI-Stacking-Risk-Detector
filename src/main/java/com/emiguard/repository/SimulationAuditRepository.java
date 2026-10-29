package com.emiguard.repository;

import com.emiguard.model.SimulationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationAuditRepository extends JpaRepository<SimulationAudit, String> {
    List<SimulationAudit> findAllByOrderByCreatedAtDesc();
}
