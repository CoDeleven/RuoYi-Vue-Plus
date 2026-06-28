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

    @NotNull(message = "Review id is required", groups = { EditGroup.class })
    private Long id;

    @NotNull(message = "Tour is required", groups = { AddGroup.class, EditGroup.class })
    private Long tourId;

    private Long orderId;

    private Long customerUserId;

    @NotBlank(message = "Nickname is required", groups = { AddGroup.class, EditGroup.class })
    private String nickname;

    private String avatar;

    @NotBlank(message = "Title is required", groups = { AddGroup.class, EditGroup.class })
    private String title;

    @NotBlank(message = "Content is required", groups = { AddGroup.class, EditGroup.class })
    private String content;

    @NotNull(message = "Rating is required", groups = { AddGroup.class, EditGroup.class })
    @Min(value = 1, message = "Rating must be at least 1", groups = { AddGroup.class, EditGroup.class })
    @Max(value = 5, message = "Rating cannot exceed 5", groups = { AddGroup.class, EditGroup.class })
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
