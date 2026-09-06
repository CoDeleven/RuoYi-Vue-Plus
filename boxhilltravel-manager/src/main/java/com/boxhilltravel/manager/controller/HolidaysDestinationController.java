package com.boxhilltravel.manager.controller;

import java.util.List;

import cn.hutool.core.lang.tree.Tree;
import com.boxhilltravel.core.domain.bo.HolidaysDestinationBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationVo;
import com.boxhilltravel.manager.service.IHolidaysDestinationService;
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
import org.dromara.common.core.utils.TreeBuildUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.core.domain.PageResult;

/**
 * 目的地分类
 *
 * @author Lion Li
 * @date 2026-06-27 16:41:24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/destination")
public class HolidaysDestinationController extends BaseController {

    private final IHolidaysDestinationService holidaysDestinationService;

    /**
     * 查询目的地分类列表
     */
    @SaCheckPermission("boxhilltravel_manager:destination:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysDestinationVo>> list(HolidaysDestinationBo bo, PageQuery pageQuery) {
        return R.ok(holidaysDestinationService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("boxhilltravel_manager:destination:list")
    @GetMapping("/tree")
    public R<List<Tree<Long>>> tree(HolidaysDestinationBo bo) {
        List<HolidaysDestinationVo> destinations = holidaysDestinationService.queryList(bo);
        return R.ok(TreeBuildUtils.buildMultiRoot(
            destinations,
            HolidaysDestinationVo::getId,
            HolidaysDestinationVo::getParentId,
            (node, treeNode) -> {
                String label = node.getNameEn();
                if (label == null || label.isBlank()) {
                    label = node.getName();
                }
                treeNode
                    .setId(node.getId())
                    .setParentId(node.getParentId())
                    .setName(label)
                    .setWeight(node.getSort());
            }
        ));
    }

    /**
     * 获取目的地分类详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:destination:query")
    @GetMapping("/{id}")
    public R<HolidaysDestinationVo> getInfo(@NotNull(message = "{boxhilltravel.validation.primaryKey.required}")
                                     @PathVariable Long id) {
        return R.ok(holidaysDestinationService.queryById(id));
    }

    /**
     * 新增目的地分类
     */
    @SaCheckPermission("boxhilltravel_manager:destination:add")
    @Log(title = "Destination", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysDestinationBo bo) {
        return toAjax(holidaysDestinationService.insertByBo(bo));
    }

    /**
     * 修改目的地分类
     */
    @SaCheckPermission("boxhilltravel_manager:destination:edit")
    @Log(title = "Destination", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysDestinationBo bo) {
        return toAjax(holidaysDestinationService.updateByBo(bo));
    }



    /**
     * 删除目的地分类
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:destination:remove")
    @Log(title = "Destination", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{boxhilltravel.validation.primaryKey.required}")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysDestinationService.deleteWithValidByIds(List.of(ids), true));
    }
}


