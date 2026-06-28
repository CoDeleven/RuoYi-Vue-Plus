package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysTourItineraryActivityBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourItineraryActivityVo;
import com.boxhilltravel.manager.service.IHolidaysTourItineraryActivityService;
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
 * 行程活动
 *
 * @author Lion Li
 * @date 2026-06-28 12:58:35
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/tour_itinerary_activity")
public class HolidaysTourItineraryActivityController extends BaseController {

    private final IHolidaysTourItineraryActivityService holidaysTourItineraryActivityService;

    /**
     * 查询行程活动列表
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary_activity:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysTourItineraryActivityVo>> list(HolidaysTourItineraryActivityBo bo, PageQuery pageQuery) {
        return R.ok(holidaysTourItineraryActivityService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出行程活动列表
     */

    /**
     * 获取行程活动详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary_activity:query")
    @GetMapping("/{id}")
    public R<HolidaysTourItineraryActivityVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(holidaysTourItineraryActivityService.queryById(id));
    }

    /**
     * 新增行程活动
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary_activity:add")
    @Log(title = "行程活动", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysTourItineraryActivityBo bo) {
        return toAjax(holidaysTourItineraryActivityService.insertByBo(bo));
    }

    /**
     * 修改行程活动
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary_activity:edit")
    @Log(title = "行程活动", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysTourItineraryActivityBo bo) {
        return toAjax(holidaysTourItineraryActivityService.updateByBo(bo));
    }



    /**
     * 删除行程活动
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary_activity:remove")
    @Log(title = "行程活动", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysTourItineraryActivityService.deleteWithValidByIds(List.of(ids), true));
    }
}


