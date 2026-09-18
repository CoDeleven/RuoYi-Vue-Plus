package com.boxhilltravel.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.boxhilltravel.core.domain.bo.HolidaysReviewBo;
import com.boxhilltravel.core.domain.vo.HolidaysReviewVo;
import com.boxhilltravel.manager.service.IHolidaysReviewService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tour review manager controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/review")
public class HolidaysReviewController extends BaseController {

    private final IHolidaysReviewService holidaysReviewService;

    @SaCheckPermission("boxhilltravel_manager:review:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysReviewVo>> list(HolidaysReviewBo bo, PageQuery pageQuery) {
        return R.ok(holidaysReviewService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("boxhilltravel_manager:review:query")
    @GetMapping("/{id}")
    public R<HolidaysReviewVo> getInfo(@NotNull(message = "{boxhilltravel.validation.reviewId.required}") @PathVariable Long id) {
        return R.ok(holidaysReviewService.queryById(id));
    }

    @SaCheckPermission("boxhilltravel_manager:review:add")
    @Log(title = "Review", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysReviewBo bo) {
        return toAjax(holidaysReviewService.insertByBo(bo));
    }

    @SaCheckPermission("boxhilltravel_manager:review:edit")
    @Log(title = "Review", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysReviewBo bo) {
        return toAjax(holidaysReviewService.updateByBo(bo));
    }

    @SaCheckPermission("boxhilltravel_manager:review:audit")
    @Log(title = "Review audit", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public R<Void> audit(@Validated @RequestBody ReviewAuditBo bo) {
        return toAjax(holidaysReviewService.audit(bo.getId(), bo.getStatus(), bo.getRejectReason()));
    }

    @SaCheckPermission("boxhilltravel_manager:review:edit")
    @Log(title = "Review featured", businessType = BusinessType.UPDATE)
    @PutMapping("/changeFeatured")
    public R<Void> changeFeatured(@Validated @RequestBody ReviewFeaturedBo bo) {
        return toAjax(holidaysReviewService.updateFeatured(bo.getId(), bo.getFeatured()));
    }

    @SaCheckPermission("boxhilltravel_manager:review:remove")
    @Log(title = "Review", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{boxhilltravel.validation.reviewIds.required}") @PathVariable Long[] ids) {
        return toAjax(holidaysReviewService.deleteWithValidByIds(List.of(ids), true));
    }

    @Data
    public static class ReviewAuditBo {
        @NotNull(message = "{boxhilltravel.validation.reviewId.required}")
        private Long id;

        @NotNull(message = "{boxhilltravel.validation.auditStatus.required}")
        private Integer status;

        private String rejectReason;
    }

    @Data
    public static class ReviewFeaturedBo {
        @NotNull(message = "{boxhilltravel.validation.reviewId.required}")
        private Long id;

        @NotNull(message = "{boxhilltravel.validation.featuredStatus.required}")
        private Integer featured;
    }

}
