package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer profile extension for sys_user.
 */
@Data
@TableName("customer_profile")
public class CustomerProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "customer_user_id", type = IdType.INPUT)
    private Long customerUserId;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
