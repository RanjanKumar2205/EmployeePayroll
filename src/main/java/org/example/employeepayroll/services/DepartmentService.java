package org.example.employeepayroll.services;

import org.example.employeepayroll.mappers.DepartmentMapper;
import org.example.employeepayroll.dtos.DepartmentResponseDto;
import org.example.employeepayroll.dtos.DepartmentRequestDto;
import org.example.employeepayroll.entities.Department;
import org.example.employeepayroll.entities.Status;
import org.example.employeepayroll.exceptions.ResourceNotFoundException;
import org.example.employeepayroll.repositories.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class DepartmentService {
    
    public static final String DEPARTMENT_NOT_FOUND = "Department with id: %s not found";
    
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    
    public DepartmentService(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    public Collection<DepartmentResponseDto> getDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    public DepartmentResponseDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(DEPARTMENT_NOT_FOUND, id))
        );
        return departmentMapper.toResponse(department);
    }

    public DepartmentResponseDto addDepartment(DepartmentRequestDto departmentRequestDto) {
        Department department = departmentMapper.toEntity(departmentRequestDto);
        departmentRepository.save(department);
        return departmentMapper.toResponse(department);
    }

    public DepartmentResponseDto putDepartment(Long id, DepartmentRequestDto departmentRequestDto) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(DEPARTMENT_NOT_FOUND, id))
        );
        departmentMapper.updateEntity(departmentRequestDto, department);
        department = departmentRepository.save(department);
        return departmentMapper.toResponse(department);
    }

    public DepartmentResponseDto patchDepartment(Long id, DepartmentRequestDto departmentRequestDto) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(DEPARTMENT_NOT_FOUND, id))
        );
        applyPatch(departmentRequestDto, department);
        department = departmentRepository.save(department);
        return departmentMapper.toResponse(department);
    }

    private void applyPatch(DepartmentRequestDto departmentRequestDto, Department department) {
        if(departmentRequestDto.getName() != null) department.setName(departmentRequestDto.getName());
        if(departmentRequestDto.getCode() != null) department.setCode(departmentRequestDto.getCode());
    }

    public DepartmentResponseDto deleteDepartmentById(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(DEPARTMENT_NOT_FOUND, id))
        );
        department.setStatus(Status.DELETE);
        department = departmentRepository.save(department);
        return departmentMapper.toResponse(department);
    }
}
