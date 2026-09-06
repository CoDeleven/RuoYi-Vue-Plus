package com.boxhilltravel.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.boxhilltravel.core.domain.bo.HolidaysDestinationPageContentBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationPageContentVo;
import com.boxhilltravel.manager.service.IHolidaysDestinationPageContentService;
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
 * Destination Page Content management.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/destination_page_content")
public class HolidaysDestinationPageContentController extends BaseController {

    private final IHolidaysDestinationPageContentService destinationPageContentService;

    @SaCheckPermission("boxhilltravel_manager:destination_page_content:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysDestinationPageContentVo>> list(HolidaysDestinationPageContentBo bo, PageQuery pageQuery) {
        return R.ok(destinationPageContentService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("boxhilltravel_manager:destination_page_content:query")
    @GetMapping("/{id}")
    public R<HolidaysDestinationPageContentVo> getInfo(@NotNull(message = "{boxhilltravel.validation.id.required}") @PathVariable Long id) {
        return R.ok(destinationPageContentService.queryById(id));
    }

    @SaCheckPermission("boxhilltravel_manager:destination_page_content:add")
    @Log(title = "Destination Page Content", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysDestinationPageContentBo bo) {
        return toAjax(destinationPageContentService.insertByBo(bo));
    }

    @SaCheckPermission("boxhilltravel_manager:destination_page_content:edit")
    @Log(title = "Destination Page Content", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysDestinationPageContentBo bo) {
        return toAjax(destinationPageContentService.updateByBo(bo));
    }

    @SaCheckPermission("boxhilltravel_manager:destination_page_content:edit")
    @Log(title = "Destination Page Content Status", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@Validated @RequestBody ChangeStatusBo bo) {
        return toAjax(destinationPageContentService.updateStatus(bo.getId(), bo.getStatus()));
    }

    @SaCheckPermission("boxhilltravel_manager:destination_page_content:remove")
    @Log(title = "Destination Page Content", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{boxhilltravel.validation.id.required}") @PathVariable Long[] ids) {
        return toAjax(destinationPageContentService.deleteWithValidByIds(List.of(ids), true));
    }

    @Data
    public static class ChangeStatusBo {
        @NotNull(message = "{boxhilltravel.validation.id.required}")
        private Long id;

        @NotNull(message = "{boxhilltravel.validation.status.required}")
        private Long status;
    }

}
