package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCustomerPasswordBo {

    private String currentPassword;

    private String emailCode;

    @NotBlank(message = "New password cannot be empty")
    @Size(min = 5, max = 30, message = "Password must be between 5 and 30 characters")
    private String newPassword;
}
