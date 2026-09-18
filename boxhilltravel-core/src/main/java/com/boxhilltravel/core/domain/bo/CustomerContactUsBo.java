package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.CustomerContactUs;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Customer contact us business object.
 */
@Data
@AutoMapper(target = CustomerContactUs.class, reverseConvertGenerate = false)
public class CustomerContactUsBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Full name is required", groups = { AddGroup.class })
    @Size(max = 100, message = "Full name cannot exceed 100 characters", groups = { AddGroup.class })
    private String fullName;

    @NotBlank(message = "Email address is required", groups = { AddGroup.class })
    @Email(message = "Invalid email address", groups = { AddGroup.class })
    @Size(max = 100, message = "Email address cannot exceed 100 characters", groups = { AddGroup.class })
    private String emailAddress;

    private String phoneNumber;

    private String enquiryType;

    @NotBlank(message = "Message is required", groups = { AddGroup.class })
    @Size(max = 2000, message = "Message cannot exceed 2000 characters", groups = { AddGroup.class })
    private String message;

    private Integer readStatus;

    private LocalDateTime readTime;

    private String ipAddress;

    private Map<String, Object> params = new HashMap<>();

}
