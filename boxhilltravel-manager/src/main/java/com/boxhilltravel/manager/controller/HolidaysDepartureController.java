package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysDepartureBo;
import com.boxhilltravel.core.domain.vo.HolidaysDepartureVo;
import com.boxhilltravel.manager.service.IHolidaysDepartureService;
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
 * 团期
 *
 * @author Lion Li
 * @date 2026-06-30 14:47:38
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/departure")
public class HolidaysDepartureController extends BaseController {

    private final IHolidaysDepartureService holidaysDepartureService;

    /**
     * 查询团期列表
     */
    @SaCheckPermission("boxhilltravel_manager:departure:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysDepartureVo>> list(HolidaysDepartureBo bo, PageQuery pageQuery) {
        return R.ok(holidaysDepartureService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出团期列表
     */

    /**
     * 获取团期详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:departure:query")
    @GetMapping("/{id}")
    public R<HolidaysDepartureVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(holidaysDepartureService.queryById(id));
    }

    /**
     * 新增团期
     */
    @SaCheckPermission("boxhilltravel_manager:departure:add")
    @Log(title = "团期", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysDepartureBo bo) {
        return toAjax(holidaysDepartureService.insertByBo(bo));
    }

    /**
     * 修改团期
     */
    @SaCheckPermission("boxhilltravel_manager:departure:edit")
    @Log(title = "团期", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysDepartureBo bo) {
        return toAjax(holidaysDepartureService.updateByBo(bo));
    }



    /**
     * 删除团期
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:departure:remove")
    @Log(title = "团期", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysDepartureService.deleteWithValidByIds(List.of(ids), true));
    }
}


