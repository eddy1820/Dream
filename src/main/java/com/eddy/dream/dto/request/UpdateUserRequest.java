package com.eddy.dream.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(description = "Update user information request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Schema(description = "User email address", example = "newemail@example.com")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Schema(description = "User phone number", example = "+886912345678")
    @Pattern(
        regexp = "^[+]?[0-9]{10,20}$",
        message = "Phone number must be 10-20 digits, optionally starting with +"
    )
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;
}

