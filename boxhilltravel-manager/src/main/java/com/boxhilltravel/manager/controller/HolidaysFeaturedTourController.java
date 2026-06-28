package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysFeaturedTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysFeaturedTourVo;
import com.boxhilltravel.manager.service.IHolidaysFeaturedTourService;
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
 * 精选线路
 *
 * @author Lion Li
 * @date 2026-06-29 00:06:41
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/featured_tour")
public class HolidaysFeaturedTourController extends BaseController {

    private final IHolidaysFeaturedTourService holidaysFeaturedTourService;

    /**
     * 查询精选线路列表
     */
    @SaCheckPermission("boxhilltravel_manager:featured_tour:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysFeaturedTourVo>> list(HolidaysFeaturedTourBo bo, PageQuery pageQuery) {
        return R.ok(holidaysFeaturedTourService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出精选线路列表
     */

    /**
     * 获取精选线路详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:featured_tour:query")
    @GetMapping("/{id}")
    public R<HolidaysFeaturedTourVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(holidaysFeaturedTourService.queryById(id));
    }

    /**
     * 新增精选线路
     */
    @SaCheckPermission("boxhilltravel_manager:featured_tour:add")
    @Log(title = "精选线路", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysFeaturedTourBo bo) {
        return toAjax(holidaysFeaturedTourService.insertByBo(bo));
    }

    /**
     * 修改精选线路
     */
    @SaCheckPermission("boxhilltravel_manager:featured_tour:edit")
    @Log(title = "精选线路", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysFeaturedTourBo bo) {
        return toAjax(holidaysFeaturedTourService.updateByBo(bo));
    }


    /**
     * 调整精选线路排序
     */
    @SaCheckPermission("boxhilltravel_manager:featured_tour:edit")
    @Log(title = "精选线路", businessType = BusinessType.UPDATE)
    @PutMapping("/updateSort")
    public R<Void> updateSort(@RequestBody HolidaysFeaturedTourBo bo) {
        return toAjax(holidaysFeaturedTourService.updateSort(bo.getId(), bo.getSortOrder()));
    }

    /**
     * 删除精选线路
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:featured_tour:remove")
    @Log(title = "精选线路", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysFeaturedTourService.deleteWithValidByIds(List.of(ids), true));
    }
}


