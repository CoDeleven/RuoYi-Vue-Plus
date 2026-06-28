package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysHotDealTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysHotDealTourVo;
import com.boxhilltravel.manager.service.IHolidaysHotDealTourService;
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
 * 热卖线路
 *
 * @author Lion Li
 * @date 2026-06-28 23:38:47
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel-manager/hot_deal_tour")
public class HolidaysHotDealTourController extends BaseController {

    private final IHolidaysHotDealTourService holidaysHotDealTourService;

    /**
     * 查询热卖线路列表
     */
    @SaCheckPermission("boxhilltravel-manager:hot_deal_tour:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysHotDealTourVo>> list(HolidaysHotDealTourBo bo, PageQuery pageQuery) {
        return R.ok(holidaysHotDealTourService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出热卖线路列表
     */

    /**
     * 获取热卖线路详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel-manager:hot_deal_tour:query")
    @GetMapping("/{id}")
    public R<HolidaysHotDealTourVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(holidaysHotDealTourService.queryById(id));
    }

    /**
     * 新增热卖线路
     */
    @SaCheckPermission("boxhilltravel-manager:hot_deal_tour:add")
    @Log(title = "热卖线路", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysHotDealTourBo bo) {
        return toAjax(holidaysHotDealTourService.insertByBo(bo));
    }

    /**
     * 修改热卖线路
     */
    @SaCheckPermission("boxhilltravel-manager:hot_deal_tour:edit")
    @Log(title = "热卖线路", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysHotDealTourBo bo) {
        return toAjax(holidaysHotDealTourService.updateByBo(bo));
    }



    /**
     * 删除热卖线路
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel-manager:hot_deal_tour:remove")
    @Log(title = "热卖线路", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysHotDealTourService.deleteWithValidByIds(List.of(ids), true));
    }
}


