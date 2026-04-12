package org.example.employeepayroll.specifications;

import jakarta.persistence.criteria.Join;
import org.example.employeepayroll.entities.Department;
import org.example.employeepayroll.entities.Employee;
import org.example.employeepayroll.entities.Status;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {
    private EmployeeSpecification() {}

    public static Specification<Employee> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern)
            );
        };
    }

    public static Specification<Employee> hasDepartment(String deptName) {
        return (root, query, cb) -> {
            if (deptName == null || deptName.isBlank()) return null;
            Join<Employee, Department> dept = root.join("department");
            return cb.like(cb.lower(dept.get("name")), "%" + deptName.toLowerCase() + "%");
        };
    }

    public static Specification<Employee> hasStatus(Status status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }
}
