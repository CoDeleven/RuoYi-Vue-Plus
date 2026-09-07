package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.CustomerContactUs;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.core.utils.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer contact us view object.
 */
@Data
@AutoMapper(target = CustomerContactUs.class)
public class CustomerContactUsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String fullName;

    private String emailAddress;

    private String phoneNumber;

    private String enquiryType;

    private String message;

    private String ipAddress;

    private Integer readStatus;

    private LocalDateTime readTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public String getReadStatusText() {
        return Integer.valueOf(1).equals(readStatus) ? "Read" : "Unread";
    }

    public boolean isRead() {
        return Integer.valueOf(1).equals(readStatus);
    }

    public boolean hasMessage() {
        return StringUtils.isNotBlank(message);
    }

}
