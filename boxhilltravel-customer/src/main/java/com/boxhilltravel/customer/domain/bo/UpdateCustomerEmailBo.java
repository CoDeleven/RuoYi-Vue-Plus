package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCustomerEmailBo {

    @NotBlank(message = "New email address cannot be empty")
    @Email(message = "Invalid email address")
    @Size(max = 50, message = "Email address cannot exceed 50 characters")
    private String newEmail;

    private String oldEmailCode;

    @NotBlank(message = "New email verification code cannot be empty")
    private String newEmailCode;
}
