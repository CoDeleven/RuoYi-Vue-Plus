package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Customer contact us record.
 */
@Data
@TableName("holidays_customer_contact_us")
public class CustomerContactUs {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String fullName;

    private String emailAddress;

    private String phoneNumber;

    private String enquiryType;

    private String message;

    private String ipAddress;

    /**
     * 0 unread, 1 read.
     */
    private Integer readStatus;

    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
