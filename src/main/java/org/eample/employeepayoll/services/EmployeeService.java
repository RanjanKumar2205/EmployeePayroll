package org.eample.employeepayoll.services;

import org.eample.employeepayoll.entities.Employee;
import org.eample.employeepayoll.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findEmployeeById(id);
    }

    public Employee addEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee patchEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void deleteEmployeeById(Long id) {
        employeeRepository.deleteEmployeeById(id);
    }

    public Collection<Employee> getEmployees() {
        return employeeRepository.findAll();
    }
}
