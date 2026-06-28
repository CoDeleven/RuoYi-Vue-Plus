package com.boxhilltravel.manager.domain.vo;

import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer user manager list view object.
 */
@Data
public class CustomerUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String userName;

    private String nickName;

    private String userType;

    private String email;

    private String phoneNumber;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private Long avatar;

    private String avatarUrl;

    private String profileNickname;

    private String contactEmail;

    private String contactPhone;

    private String status;

    private String loginIp;

    private LocalDateTime loginDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
