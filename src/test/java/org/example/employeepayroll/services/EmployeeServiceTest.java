package org.example.employeepayroll.services;

import org.example.employeepayroll.dtos.EmployeeRequestDto;
import org.example.employeepayroll.dtos.EmployeeResponseDto;
import org.example.employeepayroll.entities.Department;
import org.example.employeepayroll.entities.Employee;
import org.example.employeepayroll.entities.EmployeeType;
import org.example.employeepayroll.entities.Status;
import org.example.employeepayroll.exceptions.ResourceNotFoundException;
import org.example.employeepayroll.mappers.EmployeeMapper;
import org.example.employeepayroll.repositories.DepartmentRepository;
import org.example.employeepayroll.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// No Spring context — pure unit tests using Mockito only
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private EmployeeService employeeService;

    // ── Test fixtures ──────────────────────────────────────────────────────────

    private Department department;
    private Employee employee;
    private EmployeeRequestDto requestDto;
    private EmployeeResponseDto responseDto;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Engineering")
                .code("ENG")
                .build();

        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .phoneNumber("9876543210")
                .designation("Software Engineer")
                .employeeType(EmployeeType.FULL_TIME)
                .dateOfJoining(LocalDate.of(2024, 1, 15))
                .status(Status.ACTIVE)
                .department(department)
                .build();

        requestDto = EmployeeRequestDto.builder()
                .employeeCode("EMP001")
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .phoneNumber("9876543210")
                .designation("Software Engineer")
                .employeeType(EmployeeType.FULL_TIME)
                .dateOfJoining(LocalDate.of(2024, 1, 15))
                .departmentId(1L)
                .build();

        responseDto = EmployeeResponseDto.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .designation("Software Engineer")
                .employeeType(EmployeeType.FULL_TIME)
                .status(Status.ACTIVE)
                .departmentId(1L)
                .departmentName("Engineering")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── Test 1: createEmployee — verify repository.save() called with correct entity ──

    @Test
    @DisplayName("addEmployee: saves entity and returns mapped response DTO")
    void addEmployee_savesEntityAndReturnsResponseDto() {
        // Arrange
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeMapper.toEntity(requestDto)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(responseDto);
        doNothing().when(auditLogService).logCreate(any(), any());

        // Act
        EmployeeResponseDto result = employeeService.addEmployee(requestDto);

        // Assert — save() must be called exactly once with the mapped entity
        ArgumentCaptor<Employee> savedCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository, times(1)).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getEmail()).isEqualTo("rahul.sharma@example.com");

        // Response DTO must match what the mapper returned
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Rahul");
        assertThat(result.getDepartmentName()).isEqualTo("Engineering");
    }

    // ── Test 2: getById found — returns correct DTO ───────────────────────────

    @Test
    @DisplayName("getEmployeeById: returns response DTO when employee exists")
    void getEmployeeById_found_returnsResponseDto() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(responseDto);

        // Act
        EmployeeResponseDto result = employeeService.getEmployeeById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("rahul.sharma@example.com");
        verify(employeeRepository, times(1)).findById(1L);
    }

    // ── Test 3: getById not found — throws ResourceNotFoundException ──────────

    @Test
    @DisplayName("getEmployeeById: throws ResourceNotFoundException when employee does not exist")
    void getEmployeeById_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        // Mapper must never be called — no entity to map
        verify(employeeMapper, never()).toResponse(any());
    }

    // ── Test 4: updateEmployee (PUT) — verify save() called with updated data ─

    @Test
    @DisplayName("putEmployee: saves updated entity and returns updated response DTO")
    void putEmployee_savesUpdatedEntityAndReturnsDto() {
        // Arrange — requestDto has updated designation
        EmployeeRequestDto updateRequest = EmployeeRequestDto.builder()
                .employeeCode("EMP001")
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .phoneNumber("9876543210")
                .designation("Senior Software Engineer")   // changed
                .employeeType(EmployeeType.FULL_TIME)
                .dateOfJoining(LocalDate.of(2024, 1, 15))
                .departmentId(1L)
                .managerId(null)
                .build();

        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .designation("Senior Software Engineer")
                .employeeType(EmployeeType.FULL_TIME)
                .status(Status.ACTIVE)
                .department(department)
                .build();

        EmployeeResponseDto updatedResponse = EmployeeResponseDto.builder()
                .id(1L)
                .designation("Senior Software Engineer")
                .departmentId(1L)
                .departmentName("Engineering")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        // managerId is null — no manager lookup expected
        when(employeeRepository.save(employee)).thenReturn(updatedEmployee);
        when(employeeMapper.toResponse(updatedEmployee)).thenReturn(updatedResponse);
        doNothing().when(auditLogService).logChanges(any(), any(), any(), any());
        doNothing().when(auditLogService).logFkChange(any(), any(), any(), any(), any());

        // Act
        EmployeeResponseDto result = employeeService.putEmployee(1L, updateRequest);

        // Assert — save() must be called once and the response reflects the new designation
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(employeeMapper, times(1)).updateEntity(eq(updateRequest), eq(employee));
        assertThat(result.getDesignation()).isEqualTo("Senior Software Engineer");
    }

    // ── Test 5: deleteEmployee — verify status set to DELETE, save() called ───

    @Test
    @DisplayName("deleteEmployeeById: sets status to DELETE, calls save(), returns DTO")
    void deleteEmployeeById_setsStatusToDeleteAndSaves() {
        // Arrange
        EmployeeResponseDto deletedResponse = EmployeeResponseDto.builder()
                .id(1L)
                .status(Status.DELETE)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(deletedResponse);
        doNothing().when(auditLogService).logDelete(any(), any());

        // Act
        EmployeeResponseDto result = employeeService.deleteEmployeeById(1L);

        // Assert — entity status must be DELETE before save is called
        ArgumentCaptor<Employee> savedCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository, times(1)).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getStatus()).isEqualTo(Status.DELETE);

        // Response must reflect deleted status
        assertThat(result.getStatus()).isEqualTo(Status.DELETE);

        // Audit log for delete must fire
        verify(auditLogService, times(1)).logDelete(eq("Employee"), eq(1L));
    }
}