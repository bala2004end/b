package com.aidb.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequest {
    @NotBlank(message = "Host is required")
    private String host;

    @NotNull(message = "Port is required")
    private Integer port;

    @NotBlank(message = "Username is required")
    private String username;

    private String password;

    @NotBlank(message = "Database name is required")
    private String databaseName;
    
    private Boolean isReadOnly;
}
