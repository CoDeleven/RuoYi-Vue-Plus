package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.boxhilltravel.core.domain.HolidaysTourItineraryActivity;
import com.boxhilltravel.core.domain.bo.HolidaysTourItineraryActivityBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourItineraryActivityVo;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryActivityMapper;
import com.boxhilltravel.manager.service.IHolidaysTourItineraryActivityService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 行程活动Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-28 12:58:35
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysTourItineraryActivityServiceImpl implements IHolidaysTourItineraryActivityService {

    private final HolidaysTourItineraryActivityMapper holidaysTourItineraryActivityMapper;

/**
     * 查询行程活动
     *
     * @param id 主键
     * @return 行程活动
     */
    @Override
    public HolidaysTourItineraryActivityVo queryById(Long id) {
        return holidaysTourItineraryActivityMapper.selectVoById(id);
    }

    /**
     * 分页查询行程活动列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 行程活动分页列表
     */
    @Override
    public PageResult<HolidaysTourItineraryActivityVo> queryPageList(HolidaysTourItineraryActivityBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysTourItineraryActivity> lqw = buildQueryWrapper(bo);
        Page<HolidaysTourItineraryActivityVo> result = holidaysTourItineraryActivityMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的行程活动列表
     *
     * @param bo 查询条件
     * @return 行程活动列表
     */
    @Override
    public List<HolidaysTourItineraryActivityVo> queryList(HolidaysTourItineraryActivityBo bo) {
        LambdaQueryWrapper<HolidaysTourItineraryActivity> lqw = buildQueryWrapper(bo);
        return holidaysTourItineraryActivityMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<HolidaysTourItineraryActivity> buildQueryWrapper(HolidaysTourItineraryActivityBo bo) {
        return QueryBuilder.lambda(HolidaysTourItineraryActivity.class)
            .eqIfPresent(HolidaysTourItineraryActivity::getItineraryId, bo.getItineraryId())
            .eqIfPresent(HolidaysTourItineraryActivity::getTourId, bo.getTourId())
            .eqIfText(HolidaysTourItineraryActivity::getTitle, bo.getTitle())
            .eqIfText(HolidaysTourItineraryActivity::getDescription, bo.getDescription())
            .eqIfText(HolidaysTourItineraryActivity::getActivityIcon, bo.getActivityIcon())
            .eqIfText(HolidaysTourItineraryActivity::getSubtitle, bo.getSubtitle())
            .eqIfPresent(HolidaysTourItineraryActivity::getSortOrder, bo.getSortOrder())
            .eqIfPresent(HolidaysTourItineraryActivity::getShowInPreview, bo.getShowInPreview())
            .orderByAsc(HolidaysTourItineraryActivity::getId)
            .build();
    }

    /**
     * 新增行程活动
     *
     * @param bo 行程活动
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysTourItineraryActivityBo bo) {
        HolidaysTourItineraryActivity add = MapstructUtils.convert(bo, HolidaysTourItineraryActivity.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysTourItineraryActivityMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改行程活动
     *
     * @param bo 行程活动
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysTourItineraryActivityBo bo) {
        HolidaysTourItineraryActivity update = MapstructUtils.convert(bo, HolidaysTourItineraryActivity.class);
        validEntityBeforeSave(update);
        return holidaysTourItineraryActivityMapper.updateById(update) > 0;
    }



    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysTourItineraryActivity entity) {
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除行程活动信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 可在此扩展删除前业务校验
        }
        return holidaysTourItineraryActivityMapper.deleteByIds(ids) > 0;
    }
}


