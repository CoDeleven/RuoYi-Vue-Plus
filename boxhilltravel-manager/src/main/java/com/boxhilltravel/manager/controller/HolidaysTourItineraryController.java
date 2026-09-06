package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.manager.service.IHolidaysTourItineraryService;
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
import com.boxhilltravel.core.domain.vo.HolidaysTourItineraryVo;
import com.boxhilltravel.core.domain.bo.HolidaysTourItineraryBo;
import org.dromara.common.core.domain.PageResult;

/**
 * 行程
 *
 * @author Lion Li
 * @date 2026-06-27 15:34:56
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/tour_itinerary")
public class HolidaysTourItineraryController extends BaseController {

    private final IHolidaysTourItineraryService holidaysTourItineraryService;

    /**
     * 查询行程列表
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysTourItineraryVo>> list(HolidaysTourItineraryBo bo, PageQuery pageQuery) {
        return R.ok(holidaysTourItineraryService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出行程列表
     */

    /**
     * 获取行程详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary:query")
    @GetMapping("/{id}")
    public R<HolidaysTourItineraryVo> getInfo(@NotNull(message = "{boxhilltravel.validation.primaryKey.required}")
                                     @PathVariable Long id) {
        return R.ok(holidaysTourItineraryService.queryById(id));
    }

    /**
     * 新增行程
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary:add")
    @Log(title = "Itinerary", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysTourItineraryBo bo) {
        return toAjax(holidaysTourItineraryService.insertByBo(bo));
    }

    /**
     * 修改行程
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary:edit")
    @Log(title = "Itinerary", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysTourItineraryBo bo) {
        return toAjax(holidaysTourItineraryService.updateByBo(bo));
    }



    /**
     * 删除行程
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:tour_itinerary:remove")
    @Log(title = "Itinerary", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{boxhilltravel.validation.primaryKey.required}")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysTourItineraryService.deleteWithValidByIds(List.of(ids), true));
    }
}


