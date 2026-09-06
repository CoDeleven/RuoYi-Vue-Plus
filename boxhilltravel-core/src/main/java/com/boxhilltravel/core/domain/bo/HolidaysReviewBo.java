package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysReview;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Tour review business object.
 */
@Data
@AutoMapper(target = HolidaysReview.class, reverseConvertGenerate = false)
public class HolidaysReviewBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{boxhilltravel.validation.reviewId.required}", groups = { EditGroup.class })
    private Long id;

    @NotNull(message = "{boxhilltravel.validation.tourId.required}", groups = { AddGroup.class, EditGroup.class })
    private Long tourId;

    private Long orderId;

    private Long customerUserId;

    @NotBlank(message = "{boxhilltravel.validation.nickname.required}", groups = { AddGroup.class, EditGroup.class })
    private String nickname;

    private String avatar;

    @NotBlank(message = "{boxhilltravel.validation.title.required}", groups = { AddGroup.class, EditGroup.class })
    private String title;

    @NotBlank(message = "{boxhilltravel.validation.content.required}", groups = { AddGroup.class, EditGroup.class })
    private String content;

    @NotNull(message = "{boxhilltravel.validation.rating.required}", groups = { AddGroup.class, EditGroup.class })
    @Min(value = 1, message = "{boxhilltravel.validation.rating.min}", groups = { AddGroup.class, EditGroup.class })
    @Max(value = 5, message = "{boxhilltravel.validation.rating.max}", groups = { AddGroup.class, EditGroup.class })
    private Integer rating;

    private Integer source;

    private Integer status;

    private Integer featured;

    private Integer sortOrder;

    private LocalDateTime publishedAt;

    private String rejectReason;

    private String keyword;

    private String tourName;

    private Map<String, Object> params = new HashMap<>();

}
