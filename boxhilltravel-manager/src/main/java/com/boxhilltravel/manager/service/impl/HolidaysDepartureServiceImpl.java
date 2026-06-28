package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.domain.bo.HolidaysDepartureBo;
import com.boxhilltravel.core.domain.vo.HolidaysDepartureVo;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.manager.service.IHolidaysDepartureService;
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
 * 团期Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-30 14:47:38
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysDepartureServiceImpl implements IHolidaysDepartureService {

    private final HolidaysDepartureMapper holidaysDepartureMapper;

/**
     * 查询团期
     *
     * @param id 主键
     * @return 团期
     */
    @Override
    public HolidaysDepartureVo queryById(Long id) {
        return holidaysDepartureMapper.selectVoById(id);
    }

    /**
     * 分页查询团期列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 团期分页列表
     */
    @Override
    public PageResult<HolidaysDepartureVo> queryPageList(HolidaysDepartureBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysDeparture> lqw = buildQueryWrapper(bo);
        Page<HolidaysDepartureVo> result = holidaysDepartureMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的团期列表
     *
     * @param bo 查询条件
     * @return 团期列表
     */
    @Override
    public List<HolidaysDepartureVo> queryList(HolidaysDepartureBo bo) {
        LambdaQueryWrapper<HolidaysDeparture> lqw = buildQueryWrapper(bo);
        return holidaysDepartureMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<HolidaysDeparture> buildQueryWrapper(HolidaysDepartureBo bo) {
        return QueryBuilder.lambda(HolidaysDeparture.class)
            .eqIfPresent(HolidaysDeparture::getStatus, bo.getStatus())
            .eqIfPresent(HolidaysDeparture::getTourId, bo.getTourId())
            .orderByAsc(HolidaysDeparture::getId)
            .build();
    }

    /**
     * 新增团期
     *
     * @param bo 团期
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysDepartureBo bo) {
        HolidaysDeparture add = MapstructUtils.convert(bo, HolidaysDeparture.class);
        add.setBookedCount(0L);
        add.setAvailableCount(add.getMaxCapacity());
        validEntityBeforeSave(add);
        boolean flag = holidaysDepartureMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改团期
     *
     * @param bo 团期
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysDepartureBo bo) {
        HolidaysDeparture update = MapstructUtils.convert(bo, HolidaysDeparture.class);
        update.setBookedCount(null);
        update.setAvailableCount(null);
        validEntityBeforeSave(update);
        return holidaysDepartureMapper.updateById(update) > 0;
    }



    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysDeparture entity) {
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除团期信息
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
        return holidaysDepartureMapper.deleteByIds(ids) > 0;
    }
}


