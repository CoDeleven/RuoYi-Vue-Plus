package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysHomeBannerBo;
import com.boxhilltravel.core.domain.vo.HolidaysHomeBannerVo;
import com.boxhilltravel.manager.service.IHolidaysHomeBannerService;
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
 * 首页横幅
 *
 * @author BoxHillTravel
 * @date 2026-09-19
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/home_banner")
public class HolidaysHomeBannerController extends BaseController {

    private final IHolidaysHomeBannerService holidaysHomeBannerService;

    /**
     * 查询首页横幅列表
     */
    @SaCheckPermission("boxhilltravel_manager:home_banner:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysHomeBannerVo>> list(HolidaysHomeBannerBo bo, PageQuery pageQuery) {
        return R.ok(holidaysHomeBannerService.queryPageList(bo, pageQuery));
    }

    /**
     * 获取首页横幅详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:home_banner:query")
    @GetMapping("/{id}")
    public R<HolidaysHomeBannerVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(holidaysHomeBannerService.queryById(id));
    }

    /**
     * 新增首页横幅
     */
    @SaCheckPermission("boxhilltravel_manager:home_banner:add")
    @Log(title = "首页横幅", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysHomeBannerBo bo) {
        return toAjax(holidaysHomeBannerService.insertByBo(bo));
    }

    /**
     * 修改首页横幅
     */
    @SaCheckPermission("boxhilltravel_manager:home_banner:edit")
    @Log(title = "首页横幅", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysHomeBannerBo bo) {
        return toAjax(holidaysHomeBannerService.updateByBo(bo));
    }

    /**
     * 删除首页横幅
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:home_banner:remove")
    @Log(title = "首页横幅", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysHomeBannerService.deleteWithValidByIds(List.of(ids), true));
    }
}