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
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    
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
                .collect(Collectors.toList());
    }

    public DepartmentResponseDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department with id: " + id + " not found")
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
                () -> new ResourceNotFoundException("Department with id: " + id + " not found")
        );
        departmentMapper.updateEntity(departmentRequestDto, department);
        department = departmentRepository.save(department);
        return departmentMapper.toResponse(department);
    }

    public DepartmentResponseDto patchDepartment(Long id, DepartmentRequestDto departmentRequestDto) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department with id: " + id + " not found")
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
                () -> new ResourceNotFoundException("Department with id: " + id + " not found")
        );
        department.setStatus(Status.DELETE);
        department = departmentRepository.save(department);
        return departmentMapper.toResponse(department);
    }
}
