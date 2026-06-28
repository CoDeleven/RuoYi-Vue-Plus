package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.boxhilltravel.core.domain.HolidaysFeaturedTour;
import com.boxhilltravel.core.domain.bo.HolidaysFeaturedTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysFeaturedTourVo;
import com.boxhilltravel.core.mapper.HolidaysFeaturedTourMapper;
import com.boxhilltravel.manager.service.IHolidaysFeaturedTourService;
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
 * 精选线路Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-29 00:06:41
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysFeaturedTourServiceImpl implements IHolidaysFeaturedTourService {

    private final HolidaysFeaturedTourMapper holidaysFeaturedTourMapper;

/**
     * 查询精选线路
     *
     * @param id 主键
     * @return 精选线路
     */
    @Override
    public HolidaysFeaturedTourVo queryById(Long id) {
        return holidaysFeaturedTourMapper.selectVoById(id);
    }

    /**
     * 分页查询精选线路列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 精选线路分页列表
     */
    @Override
    public PageResult<HolidaysFeaturedTourVo> queryPageList(HolidaysFeaturedTourBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysFeaturedTour> lqw = buildQueryWrapper(bo);
        Page<HolidaysFeaturedTourVo> result = holidaysFeaturedTourMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的精选线路列表
     *
     * @param bo 查询条件
     * @return 精选线路列表
     */
    @Override
    public List<HolidaysFeaturedTourVo> queryList(HolidaysFeaturedTourBo bo) {
        LambdaQueryWrapper<HolidaysFeaturedTour> lqw = buildQueryWrapper(bo);
        return holidaysFeaturedTourMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<HolidaysFeaturedTour> buildQueryWrapper(HolidaysFeaturedTourBo bo) {
        return QueryBuilder.lambda(HolidaysFeaturedTour.class)
            .eqIfPresent(HolidaysFeaturedTour::getTourId, bo.getTourId())
            .orderByAsc(HolidaysFeaturedTour::getSortOrder)
            .orderByAsc(HolidaysFeaturedTour::getId)
            .build();
    }

    /**
     * 新增精选线路
     *
     * @param bo 精选线路
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysFeaturedTourBo bo) {
        HolidaysFeaturedTour add = MapstructUtils.convert(bo, HolidaysFeaturedTour.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysFeaturedTourMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改精选线路
     *
     * @param bo 精选线路
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysFeaturedTourBo bo) {
        HolidaysFeaturedTour update = MapstructUtils.convert(bo, HolidaysFeaturedTour.class);
        validEntityBeforeSave(update);
        return holidaysFeaturedTourMapper.updateById(update) > 0;
    }


    /**
     * 调整精选线路排序
     *
     * @param id 主键
     * @param sortValue 排序值
     * @return 是否修改成功
     */
    @Override
    public Boolean updateSort(Long id, Integer sortValue) {
        return holidaysFeaturedTourMapper.lambda()
            .set(HolidaysFeaturedTour::getSortOrder, sortValue)
            .eq(HolidaysFeaturedTour::getId, id)
            .update();
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysFeaturedTour entity) {
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除精选线路信息
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
        return holidaysFeaturedTourMapper.deleteByIds(ids) > 0;
    }
}


