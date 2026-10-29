package com.emiguard.repository;

import com.emiguard.model.IncomePattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomePatternRepository extends JpaRepository<IncomePattern, String> {
}
