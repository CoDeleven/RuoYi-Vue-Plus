package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysTourServiceItemBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourServiceItemVo;
import com.boxhilltravel.manager.service.IHolidaysTourServiceItemService;
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
 * 线路服务项
 *
 * @author Lion Li
 * @date 2026-06-28 21:44:19
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/tour_service_item")
public class HolidaysTourServiceItemController extends BaseController {

    private final IHolidaysTourServiceItemService holidaysTourServiceItemService;

    /**
     * 查询线路服务项列表
     */
    @SaCheckPermission("boxhilltravel_manager:tour_service_item:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysTourServiceItemVo>> list(HolidaysTourServiceItemBo bo, PageQuery pageQuery) {
        return R.ok(holidaysTourServiceItemService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出线路服务项列表
     */

    /**
     * 获取线路服务项详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:tour_service_item:query")
    @GetMapping("/{id}")
    public R<HolidaysTourServiceItemVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(holidaysTourServiceItemService.queryById(id));
    }

    /**
     * 新增线路服务项
     */
    @SaCheckPermission("boxhilltravel_manager:tour_service_item:add")
    @Log(title = "线路服务项", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysTourServiceItemBo bo) {
        return toAjax(holidaysTourServiceItemService.insertByBo(bo));
    }

    /**
     * 修改线路服务项
     */
    @SaCheckPermission("boxhilltravel_manager:tour_service_item:edit")
    @Log(title = "线路服务项", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysTourServiceItemBo bo) {
        return toAjax(holidaysTourServiceItemService.updateByBo(bo));
    }



    /**
     * 删除线路服务项
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:tour_service_item:remove")
    @Log(title = "线路服务项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysTourServiceItemService.deleteWithValidByIds(List.of(ids), true));
    }
}


