package com.boxhilltravel.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.boxhilltravel.core.domain.bo.HolidaysDestinationTagRelBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationTagRelVo;
import com.boxhilltravel.manager.service.IHolidaysDestinationTagRelService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
 * Destination Tag 管理
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/destination_tag")
public class HolidaysDestinationTagRelController extends BaseController {

    private final IHolidaysDestinationTagRelService destinationTagRelService;

    /**
     * 查询 Destination Tag 列表
     */
    @SaCheckPermission("boxhilltravel_manager:destination_tag:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysDestinationTagRelVo>> list(HolidaysDestinationTagRelBo bo, PageQuery pageQuery) {
        return R.ok(destinationTagRelService.queryPageList(bo, pageQuery));
    }

    /**
     * 获取 Destination Tag 详细信息
     */
    @SaCheckPermission("boxhilltravel_manager:destination_tag:query")
    @GetMapping("/{id}")
    public R<HolidaysDestinationTagRelVo> getInfo(@NotNull(message = "{boxhilltravel.validation.primaryKey.required}") @PathVariable Long id) {
        return R.ok(destinationTagRelService.queryById(id));
    }

    /**
     * 新增 Destination Tag
     */
    @SaCheckPermission("boxhilltravel_manager:destination_tag:add")
    @Log(title = "Destination Tag", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysDestinationTagRelBo bo) {
        return toAjax(destinationTagRelService.insertByBo(bo));
    }

    /**
     * 修改 Destination Tag
     */
    @SaCheckPermission("boxhilltravel_manager:destination_tag:edit")
    @Log(title = "Destination Tag", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysDestinationTagRelBo bo) {
        return toAjax(destinationTagRelService.updateByBo(bo));
    }

    /**
     * 调整 Destination Tag 排序
     */
    @SaCheckPermission("boxhilltravel_manager:destination_tag:edit")
    @Log(title = "Destination Tag", businessType = BusinessType.UPDATE)
    @PutMapping("/updateSort")
    public R<Void> updateSort(@RequestBody HolidaysDestinationTagRelBo bo) {
        return toAjax(destinationTagRelService.updateSort(bo.getId(), bo.getSortOrder()));
    }

    /**
     * 删除 Destination Tag
     */
    @SaCheckPermission("boxhilltravel_manager:destination_tag:remove")
    @Log(title = "Destination Tag", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{boxhilltravel.validation.primaryKey.required}") @PathVariable Long[] ids) {
        return toAjax(destinationTagRelService.deleteWithValidByIds(List.of(ids), true));
    }
}
