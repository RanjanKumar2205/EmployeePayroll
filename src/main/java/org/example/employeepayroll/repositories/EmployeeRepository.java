package org.example.employeepayroll.repositories;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.employeepayroll.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String username);
}