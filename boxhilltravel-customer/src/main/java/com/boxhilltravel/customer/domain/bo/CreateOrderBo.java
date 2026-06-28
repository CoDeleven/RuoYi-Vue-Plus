package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Create order request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateOrderBo extends CheckoutQuoteBo {

    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull(message = "Contact is required")
    private ContactBo contact;

    @Valid
    @NotEmpty(message = "Traveler information is required")
    private List<TravelerBo> travelers;

    @NotNull(message = "Terms must be accepted")
    private Boolean termsAccepted;

    private String remark;

    @Data
    public static class ContactBo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Contact name is required")
        private String name;

        @NotBlank(message = "Contact email is required")
        @Email(message = "Invalid contact email")
        private String email;

        @NotBlank(message = "Contact phone is required")
        private String phone;

        private String address;

        private String city;

        private String region;

        private String postalCode;

        private String country;

    }

    @Data
    public static class TravelerBo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Boolean primaryTraveler;

        private String title;

        @NotBlank(message = "First name is required")
        private String firstName;

        private String middleName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private Boolean noMiddleName;

        @NotNull(message = "Date of birth is required")
        private LocalDate dateOfBirth;

        @Email(message = "Invalid traveler email")
        private String email;

        private String phone;

        private String placeOfBirth;

        @NotBlank(message = "Nationality is required")
        private String nationality;

        @NotBlank(message = "Passport number is required")
        private String passportNumber;

        @NotNull(message = "Passport expiry date is required")
        private LocalDate passportExpiryDate;

        private String address;

        private String city;

        private String region;

        private String postalCode;

        private String country;

    }

}
