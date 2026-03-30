package org.eample.employeepayoll.repositories;

import org.eample.employeepayoll.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
//public class EmployeeRepository {
//    private static final Map<Integer, Employee> employees;
//
//    static {
//        employees = new HashMap<>() {
//            {
//                put(1, Employee.builder()
//                        .id(1)
//                        .employeeCode("u73")
//                        .firstName("Ranjan")
//                        .lastName("Kumar")
//                        .email("ranjan.kumar@xyz.com")
//                        .phoneNumber("999999999")
//                        .dateOfJoining(LocalDate.now())
//                        .designation("Associate")
//                        .employeeType("Permanent")
//                        .status("AC")
//                        .createdAt(LocalDateTime.now())
//                        .updatedAt(LocalDateTime.now())
//                        .build()
//                );
//                put(2, Employee.builder()
//                        .id(2)
//                        .employeeCode("u20")
//                        .firstName("Ram")
//                        .lastName("Pukar")
//                        .email("ram.pukar@xyz.com")
//                        .phoneNumber("99999999")
//                        .dateOfJoining(LocalDate.now())
//                        .designation("Executive")
//                        .employeeType("Permanent")
//                        .status("AC")
//                        .createdAt(LocalDateTime.now())
//                        .updatedAt(LocalDateTime.now())
//                        .build()
//                );
//                put(3, Employee.builder()
//                        .id(3)
//                        .employeeCode("u74")
//                        .firstName("Ved")
//                        .lastName("Vyas")
//                        .email("ved.vyas@xyz.com")
//                        .phoneNumber("9999999")
//                        .dateOfJoining(LocalDate.now())
//                        .designation("Writer")
//                        .employeeType("Contract")
//                        .status("AC")
//                        .createdAt(LocalDateTime.now())
//                        .updatedAt(LocalDateTime.now())
//                        .build()
//                );
//            }
//        };
//    }
//
//    public Optional<Employee> getEmployeeById(int id) {
//        return Optional.ofNullable(employees.get(id));
//    }
//
//    public Employee addEmployee(Employee employee) {
//        employees.put(employee.getId(), employee);
//        return employees.get(employee.getId());
//    }
//
//    public Employee patchEmployee(Employee employee) {
//        employees.put(employee.getId(), employee);
//        return employees.get(employee.getId());
//    }
//
//    public Employee deleteEmployeeById(int id) {
//        return employees.remove(id);
//    }
//
//    public Collection<Employee> getEmployees() {
//        return employees.values();
//    }
//}
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findEmployeeById(Long id);

    void deleteEmployeeById(Long id);

}