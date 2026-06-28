package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Customer social login request.
 */
@Data
public class CustomerSocialLoginBo {

    @NotBlank(message = "Client id cannot be empty")
    private String clientId;

    @NotBlank(message = "Social source cannot be empty")
    private String source;

    @NotBlank(message = "Social code cannot be empty")
    private String socialCode;

    @NotBlank(message = "Social state cannot be empty")
    private String socialState;

}
