package org.example.employeepayroll.repositories;

import org.example.employeepayroll.entities.Employee;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String username);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department")
    Page<Employee> findAll(@NonNull Pageable pageable);
}