package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourVo;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import com.boxhilltravel.manager.service.IHolidaysTourService;
import org.dromara.common.core.domain.PageResult;

/**
 * 线路管理
 *
 * @author CoDeleven
 * @date 2026-06-26 18:26:30
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/tour")
public class HolidaysTourController extends BaseController {

    private final IHolidaysTourService holidaysTourService;

    /**
     * 查询线路管理列表
     */
    @SaCheckPermission("boxhilltravel_manager:tour:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysTourVo>> list(HolidaysTourBo bo, PageQuery pageQuery) {
        return R.ok(holidaysTourService.queryPageList(bo, pageQuery));
    }

    /**
     * 获取线路管理详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:tour:query")
    @GetMapping("/{id}")
    public R<HolidaysTourVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(holidaysTourService.queryById(id));
    }

    /**
     * 新增线路管理
     */
    @SaCheckPermission("boxhilltravel_manager:tour:add")
    @Log(title = "线路管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysTourBo bo) {
        return toAjax(holidaysTourService.insertByBo(bo));
    }

    /**
     * 修改线路管理
     */
    @SaCheckPermission("boxhilltravel_manager:tour:edit")
    @Log(title = "线路管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysTourBo bo) {
        return toAjax(holidaysTourService.updateByBo(bo));
    }

    /**
     * 修改线路管理状态
     */
    @SaCheckPermission("boxhilltravel_manager:tour:edit")
    @Log(title = "线路管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody HolidaysTourBo bo) {
        return toAjax(holidaysTourService.updateStatus(bo.getId(), bo.getStatus()));
    }


    /**
     * 删除线路管理
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:tour:remove")
    @Log(title = "线路管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysTourService.deleteWithValidByIds(List.of(ids), true));
    }
}


