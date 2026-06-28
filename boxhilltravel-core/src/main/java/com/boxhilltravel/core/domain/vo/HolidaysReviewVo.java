package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysReview;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Tour review view object.
 */
@Data
@AutoMapper(target = HolidaysReview.class)
public class HolidaysReviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tourId;

    private String tourName;

    private Long orderId;

    private Long customerUserId;

    private String nickname;

    private String avatar;

    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "avatar")
    private String userAvatar;

    private String title;

    private String content;

    private Integer rating;

    private Integer source;

    private Integer status;

    private Integer featured;

    private Integer sortOrder;

    private LocalDateTime publishedAt;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

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
