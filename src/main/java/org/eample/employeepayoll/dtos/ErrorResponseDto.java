package org.eample.employeepayoll.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ErrorResponseDto {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String error;
    private String path;
}
