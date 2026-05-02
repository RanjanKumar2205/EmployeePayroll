package org.example.employeepayroll.repositories;

import org.example.employeepayroll.entities.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<SalaryStructure, Long> {
    Collection<SalaryStructure> findByEmployeeId(Long id);

    Optional<SalaryStructure> findByEmployeeIdAndIsActiveTrue(Long employeeId);
}
