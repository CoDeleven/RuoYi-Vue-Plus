package com.boxhilltravel.manager.controller;

import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysFaqItemBo;
import com.boxhilltravel.core.domain.vo.HolidaysFaqItemVo;
import com.boxhilltravel.manager.service.IHolidaysFaqItemService;
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
 * FAQ条目
 *
 * @author Lion Li
 * @date 2026-06-29 15:46:06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/faq_item")
public class HolidaysFaqItemController extends BaseController {

    private final IHolidaysFaqItemService holidaysFaqItemService;

    /**
     * 查询FAQ条目列表
     */
    @SaCheckPermission("boxhilltravel_manager:faq_item:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysFaqItemVo>> list(HolidaysFaqItemBo bo, PageQuery pageQuery) {
        return R.ok(holidaysFaqItemService.queryPageList(bo, pageQuery));
    }

    /**
     * 获取FAQ条目详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:faq_item:query")
    @GetMapping("/{id}")
    public R<HolidaysFaqItemVo> getInfo(@NotNull(message = "{boxhilltravel.validation.primaryKey.required}")
                                     @PathVariable Long id) {
        return R.ok(holidaysFaqItemService.queryById(id));
    }

    /**
     * 新增FAQ条目
     */
    @SaCheckPermission("boxhilltravel_manager:faq_item:add")
    @Log(title = "FAQ Item", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysFaqItemBo bo) {
        return toAjax(holidaysFaqItemService.insertByBo(bo));
    }

    /**
     * 修改FAQ条目
     */
    @SaCheckPermission("boxhilltravel_manager:faq_item:edit")
    @Log(title = "FAQ Item", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysFaqItemBo bo) {
        return toAjax(holidaysFaqItemService.updateByBo(bo));
    }

    /**
     * 修改FAQ条目状态
     */
    @SaCheckPermission("boxhilltravel_manager:faq_item:edit")
    @Log(title = "FAQ Item", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody HolidaysFaqItemBo bo) {
        return toAjax(holidaysFaqItemService.updateStatus(bo.getId(), bo.getStatus()));
    }


    /**
     * 删除FAQ条目
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:faq_item:remove")
    @Log(title = "FAQ Item", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{boxhilltravel.validation.primaryKey.required}")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysFaqItemService.deleteWithValidByIds(List.of(ids), true));
    }
}


