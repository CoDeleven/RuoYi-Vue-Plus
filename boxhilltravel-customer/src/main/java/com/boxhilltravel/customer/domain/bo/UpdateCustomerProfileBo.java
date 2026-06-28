package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Customer profile update request.
 */
@Data
public class UpdateCustomerProfileBo {

    @Valid
    @NotNull(message = "Contact info is required")
    private ContactInfo contactInfo;

    @Data
    public static class ContactInfo {

        private String mailingAddress;

        private String city;

        private String postalCode;

        private String region;

        private String country;

        private String contactEmail;

        private String phone;

    }

}
