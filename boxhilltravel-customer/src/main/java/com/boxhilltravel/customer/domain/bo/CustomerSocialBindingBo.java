package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerSocialBindingBo {

    @NotBlank(message = "Social source cannot be empty")
    private String source;

    @NotBlank(message = "Social code cannot be empty")
    private String socialCode;

    @NotBlank(message = "Social state cannot be empty")
    private String socialState;
}
