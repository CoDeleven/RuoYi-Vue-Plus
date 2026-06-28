package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.manager.event.TourSearchRebuildEvent;
import com.boxhilltravel.manager.service.IHolidaysTourItineraryService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.boxhilltravel.core.domain.bo.HolidaysTourItineraryBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourItineraryVo;
import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 行程Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-27 15:20:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysTourItineraryServiceImpl implements IHolidaysTourItineraryService {

    private final HolidaysTourItineraryMapper holidaysTourItineraryMapper;
    private final ApplicationEventPublisher eventPublisher;

/**
     * 查询行程
     *
     * @param id 主键
     * @return 行程
     */
    @Override
    public HolidaysTourItineraryVo queryById(Long id) {
        return holidaysTourItineraryMapper.selectVoById(id);
    }

    /**
     * 分页查询行程列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 行程分页列表
     */
    @Override
    public PageResult<HolidaysTourItineraryVo> queryPageList(HolidaysTourItineraryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysTourItinerary> lqw = buildQueryWrapper(bo);
        Page<HolidaysTourItineraryVo> result = holidaysTourItineraryMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的行程列表
     *
     * @param bo 查询条件
     * @return 行程列表
     */
    @Override
    public List<HolidaysTourItineraryVo> queryList(HolidaysTourItineraryBo bo) {
        LambdaQueryWrapper<HolidaysTourItinerary> lqw = buildQueryWrapper(bo);
        return holidaysTourItineraryMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<HolidaysTourItinerary> buildQueryWrapper(HolidaysTourItineraryBo bo) {
        return QueryBuilder.lambda(HolidaysTourItinerary.class)
            .eq(Objects.nonNull(bo.getTourId()) && bo.getTourId() > 0, HolidaysTourItinerary::getTourId, bo.getTourId())
            .orderByAsc(HolidaysTourItinerary::getId)
            .build();
    }

    /**
     * 新增行程
     *
     * @param bo 行程
     * @return 是否新增成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(HolidaysTourItineraryBo bo) {
        HolidaysTourItinerary add = MapstructUtils.convert(bo, HolidaysTourItinerary.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysTourItineraryMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            publishTourSearchRebuild(add.getTourId());
        }
        return flag;
    }

    /**
     * 修改行程
     *
     * @param bo 行程
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(HolidaysTourItineraryBo bo) {
        HolidaysTourItinerary update = MapstructUtils.convert(bo, HolidaysTourItinerary.class);
        validEntityBeforeSave(update);
        boolean flag = holidaysTourItineraryMapper.updateById(update) > 0;
        if (flag) {
            publishTourSearchRebuild(update.getTourId());
        }
        return flag;
    }



    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysTourItinerary entity) {
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除行程信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 可在此扩展删除前业务校验
        }
        Set<Long> tourIds = holidaysTourItineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
                .select(HolidaysTourItinerary::getTourId)
                .in(HolidaysTourItinerary::getId, ids))
            .stream()
            .map(HolidaysTourItinerary::getTourId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        boolean flag = holidaysTourItineraryMapper.deleteByIds(ids) > 0;
        if (flag) {
            tourIds.forEach(this::publishTourSearchRebuild);
        }
        return flag;
    }

    private void publishTourSearchRebuild(Long tourId) {
        if (tourId != null) {
            eventPublisher.publishEvent(TourSearchRebuildEvent.rebuild(tourId));
        }
    }
}


