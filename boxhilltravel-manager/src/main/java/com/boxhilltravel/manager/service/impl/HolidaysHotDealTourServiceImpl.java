package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.boxhilltravel.core.domain.HolidaysHotDealTour;
import com.boxhilltravel.core.domain.bo.HolidaysHotDealTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysHotDealTourVo;
import com.boxhilltravel.core.mapper.HolidaysHotDealTourMapper;
import com.boxhilltravel.manager.service.IHolidaysHotDealTourService;
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
 * 热卖线路Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-28 23:38:47
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysHotDealTourServiceImpl implements IHolidaysHotDealTourService {

    private final HolidaysHotDealTourMapper holidaysHotDealTourMapper;

/**
     * 查询热卖线路
     *
     * @param id 主键
     * @return 热卖线路
     */
    @Override
    public HolidaysHotDealTourVo queryById(Long id) {
        return holidaysHotDealTourMapper.selectVoById(id);
    }

    /**
     * 分页查询热卖线路列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 热卖线路分页列表
     */
    @Override
    public PageResult<HolidaysHotDealTourVo> queryPageList(HolidaysHotDealTourBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysHotDealTour> lqw = buildQueryWrapper(bo);
        Page<HolidaysHotDealTourVo> result = holidaysHotDealTourMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的热卖线路列表
     *
     * @param bo 查询条件
     * @return 热卖线路列表
     */
    @Override
    public List<HolidaysHotDealTourVo> queryList(HolidaysHotDealTourBo bo) {
        LambdaQueryWrapper<HolidaysHotDealTour> lqw = buildQueryWrapper(bo);
        return holidaysHotDealTourMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<HolidaysHotDealTour> buildQueryWrapper(HolidaysHotDealTourBo bo) {
        return QueryBuilder.lambda(HolidaysHotDealTour.class)
            .eqIfPresent(HolidaysHotDealTour::getTourId, bo.getTourId())
            .eqIfPresent(HolidaysHotDealTour::getSortOrder, bo.getSortOrder())
            .orderByAsc(HolidaysHotDealTour::getSortOrder)
            .orderByAsc(HolidaysHotDealTour::getId)
            .build();
    }

    /**
     * 新增热卖线路
     *
     * @param bo 热卖线路
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysHotDealTourBo bo) {
        HolidaysHotDealTour add = MapstructUtils.convert(bo, HolidaysHotDealTour.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysHotDealTourMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改热卖线路
     *
     * @param bo 热卖线路
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysHotDealTourBo bo) {
        HolidaysHotDealTour update = MapstructUtils.convert(bo, HolidaysHotDealTour.class);
        validEntityBeforeSave(update);
        return holidaysHotDealTourMapper.updateById(update) > 0;
    }



    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysHotDealTour entity) {
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除热卖线路信息
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
        return holidaysHotDealTourMapper.deleteByIds(ids) > 0;
    }
}


