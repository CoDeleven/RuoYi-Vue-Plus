package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.CustomerProfile;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer profile extension view object.
 */
@Data
@AutoMapper(target = CustomerProfile.class)
public class CustomerProfileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long customerUserId;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
