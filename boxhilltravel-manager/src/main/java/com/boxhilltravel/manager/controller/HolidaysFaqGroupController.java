package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysFaqGroupBo;
import com.boxhilltravel.core.domain.vo.HolidaysFaqGroupVo;
import com.boxhilltravel.manager.service.IHolidaysFaqGroupService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.core.domain.PageResult;

/**
 * FAQ分组
 *
 * @author Lion Li
 * @date 2026-06-29 15:50:16
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/faq_group")
public class HolidaysFaqGroupController extends BaseController {

    private final IHolidaysFaqGroupService holidaysFaqGroupService;

    /**
     * 查询FAQ分组列表
     */
    @SaCheckPermission("boxhilltravel_manager:faq_group:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysFaqGroupVo>> list(HolidaysFaqGroupBo bo, PageQuery pageQuery) {
        return R.ok(holidaysFaqGroupService.queryPageList(bo, pageQuery));
    }

    /**
     * 获取FAQ分组详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:faq_group:query")
    @GetMapping("/{id}")
    public R<HolidaysFaqGroupVo> getInfo(@NotNull(message = "{boxhilltravel.validation.primaryKey.required}")
                                     @PathVariable Long id) {
        return R.ok(holidaysFaqGroupService.queryById(id));
    }

    /**
     * 新增FAQ分组
     */
    @SaCheckPermission("boxhilltravel_manager:faq_group:add")
    @Log(title = "FAQ Group", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysFaqGroupBo bo) {
        return toAjax(holidaysFaqGroupService.insertByBo(bo));
    }

    /**
     * 修改FAQ分组
     */
    @SaCheckPermission("boxhilltravel_manager:faq_group:edit")
    @Log(title = "FAQ Group", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysFaqGroupBo bo) {
        return toAjax(holidaysFaqGroupService.updateByBo(bo));
    }

    /**
     * 修改FAQ分组状态
     */
    @SaCheckPermission("boxhilltravel_manager:faq_group:edit")
    @Log(title = "FAQ Group", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody HolidaysFaqGroupBo bo) {
        return toAjax(holidaysFaqGroupService.updateStatus(bo.getId(), bo.getStatus()));
    }

    /**
     * 查询启用的FAQ分组列表
     */
    @SaCheckPermission("boxhilltravel_manager:faq_group:list")
    @GetMapping("/enabledList")
    public R<List<HolidaysFaqGroupVo>> enabledList(@RequestParam(required = false) Integer module) {
        return R.ok(holidaysFaqGroupService.queryEnabledList(module));
    }

    /**
     * 删除FAQ分组
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:faq_group:remove")
    @Log(title = "FAQ Group", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{boxhilltravel.validation.primaryKey.required}")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysFaqGroupService.deleteWithValidByIds(List.of(ids), true));
    }
}


