package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Tour review object holidays_review.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_review")
public class HolidaysReview extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long tourId;

    private Long orderId;

    private Long customerUserId;

    private String nickname;

    private String avatar;

    private String title;

    private String content;

    private Integer rating;

    /**
     * 1 manager entry, 2 customer submission.
     */
    private Integer source;

    /**
     * 0 pending, 1 published, 2 rejected.
     */
    private Integer status;

    /**
     * 1 featured on home page, 0 normal.
     */
    private Integer featured;

    private Integer sortOrder;

    private LocalDateTime publishedAt;

    private String rejectReason;

}
