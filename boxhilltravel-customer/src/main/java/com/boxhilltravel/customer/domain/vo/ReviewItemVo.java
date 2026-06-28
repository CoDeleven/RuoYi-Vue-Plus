package com.boxhilltravel.customer.domain.vo;

import lombok.Data;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer-facing review item.
 */
@Data
public class ReviewItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tourId;

    private String tourName;

    private Long userId;

    private String userName;

    private String avatar;

    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "avatar")
    private String userAvatar;

    private Integer rating;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private Boolean isVerified;

    public String getUserAvatar() {
        if (StringUtils.isNotBlank(userAvatar)) {
            return userAvatar;
        }
        return isUrl(avatar) ? avatar : null;
    }

    private boolean isUrl(String value) {
        return StringUtils.isNotBlank(value) && (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("//"));
    }

}
