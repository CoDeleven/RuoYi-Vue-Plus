package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerEmailCodeBo {

    @NotBlank(message = "Verification purpose cannot be empty")
    private String purpose;

    @Email(message = "Invalid email address")
    @Size(max = 50, message = "Email address cannot exceed 50 characters")
    private String email;
}
