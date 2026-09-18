package com.boxhilltravel.manager.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.boxhilltravel.core.domain.bo.HolidaysTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourVo;
import com.boxhilltravel.manager.domain.dto.TourImportRowResult;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.io.resource.ClassPathResource;
import org.dromara.common.core.utils.file.FileUtils;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import com.boxhilltravel.manager.service.IHolidaysTourImportService;
import com.boxhilltravel.manager.service.IHolidaysTourService;
import org.dromara.common.core.domain.PageResult;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 线路管理
 *
 * @author CoDeleven
 * @date 2026-06-26 18:26:30
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/tour")
public class HolidaysTourController extends BaseController {

    private final IHolidaysTourService holidaysTourService;
    private final IHolidaysTourImportService holidaysTourImportService;

    /**
     * 查询线路管理列表
     */
    @SaCheckPermission("boxhilltravel_manager:tour:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysTourVo>> list(HolidaysTourBo bo, PageQuery pageQuery) {
        return R.ok(holidaysTourService.queryPageList(bo, pageQuery));
    }

    /**
     * 获取线路管理详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("boxhilltravel_manager:tour:query")
    @GetMapping("/{id}")
    public R<HolidaysTourVo> getInfo(@NotNull(message = "{boxhilltravel.validation.primaryKey.required}")
                                     @PathVariable Long id) {
        return R.ok(holidaysTourService.queryById(id));
    }

    /**
     * 新增线路管理
     */
    @SaCheckPermission("boxhilltravel_manager:tour:add")
    @Log(title = "Tour", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HolidaysTourBo bo) {
        return toAjax(holidaysTourService.insertByBo(bo));
    }

    /**
     * 修改线路管理
     */
    @SaCheckPermission("boxhilltravel_manager:tour:edit")
    @Log(title = "Tour", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HolidaysTourBo bo) {
        return toAjax(holidaysTourService.updateByBo(bo));
    }

    /**
     * 修改线路管理状态
     */
    @SaCheckPermission("boxhilltravel_manager:tour:edit")
    @Log(title = "Tour", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody HolidaysTourBo bo) {
        return toAjax(holidaysTourService.updateStatus(bo.getId(), bo.getStatus()));
    }


    /**
     * 删除线路管理
     *
     * @param ids 主键串
     */
    @SaCheckPermission("boxhilltravel_manager:tour:remove")
    @Log(title = "Tour", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{boxhilltravel.validation.primaryKey.required}")
                          @PathVariable Long[] ids) {
        return toAjax(holidaysTourService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 批量导入线路（Tours / Destinations / Itinerary / ServiceItems / Departures 多工作表模板）
     */
    @SaCheckPermission("boxhilltravel_manager:tour:add")
    @Log(title = "线路管理导入", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importData(@RequestPart("file") MultipartFile file) {
        List<TourImportRowResult> results = holidaysTourImportService.importTours(file);
        long successCount = results.stream().filter(TourImportRowResult::isSuccess).count();
        long failCount = results.size() - successCount;
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(results.size()).append(" 个线路，成功 ").append(successCount)
            .append(" 个，失败 ").append(failCount).append(" 个");
        if (failCount > 0) {
            sb.append("<br/>");
            results.stream()
                .filter(r -> !r.isSuccess())
                .forEach(r -> sb.append("第").append(r.getRowNumber()).append("行(")
                    .append(r.getTourCode()).append(")：").append(r.getMessage()).append("<br/>"));
        }
        return failCount == 0 ? R.ok(sb.toString()) : R.fail(sb.toString());
    }

    /**
     * 下载线路批量导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ClassPathResource resource = new ClassPathResource("import-templates/tours_import_template.xlsx");
        FileUtils.setAttachmentResponseHeader(response, "tours_import_template.xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        try (InputStream is = resource.getStream()) {
            is.transferTo(response.getOutputStream());
        }
    @Resource
    private TourImageMigrationService migrationService;

    @PostMapping("/migrate")
    public R<MigrationSummary> migrate(@RequestBody TourImageMigrationOptions options) {
        options.setReportFile(Paths.get("script", "logs", "tour-image-migration-" + System.currentTimeMillis() + ".csv"));
        MigrationSummary summary = migrationService.migrate(options);
        return R.ok(summary);
    }
}


