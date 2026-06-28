package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Customer review creation request.
 */
@Data
public class CreateReviewBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Order is required")
    private Long orderId;

    @NotNull(message = "Tour is required")
    private Long tourId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

}
