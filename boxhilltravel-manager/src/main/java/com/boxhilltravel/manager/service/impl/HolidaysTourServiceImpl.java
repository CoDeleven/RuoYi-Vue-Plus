package com.boxhilltravel.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import com.boxhilltravel.core.domain.HolidaysTourTagRel;
import com.boxhilltravel.core.domain.bo.HolidaysTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourVo;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryMapper;
import com.boxhilltravel.core.mapper.HolidaysTourMapper;
import com.boxhilltravel.core.mapper.HolidaysTourTagRelMapper;
import com.boxhilltravel.manager.event.TourSearchRebuildEvent;
import com.boxhilltravel.manager.service.IHolidaysTourService;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.system.domain.SysDictData;
import org.dromara.system.mapper.SysDictDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 线路管理Service业务层处理
 *
 * @author CoDeleven
 * @date 2026-06-26 18:26:30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysTourServiceImpl implements IHolidaysTourService {

    private static final String COLLECTION_DICT_TYPE = "holidays_tour_collection";
    private static final long DESTINATION_LEVEL_COUNTRY = 2L;
    private static final long DESTINATION_LEVEL_CITY = 3L;

    private final HolidaysTourMapper holidaysTourMapper;
    private final HolidaysTourTagRelMapper holidaysTourTagRelMapper;
    private final HolidaysTourItineraryMapper itineraryMapper;
    private final HolidaysDestinationMapper destinationMapper;
    private final SysDictDataMapper sysDictDataMapper;
    private final ApplicationEventPublisher eventPublisher;

/**
     * 查询线路管理
     *
     * @param id 主键
     * @return 线路管理
     */
    @Override
    public HolidaysTourVo queryById(Long id) {
        HolidaysTourVo holidaysTourVo = holidaysTourMapper.selectVoById(id);
        if (holidaysTourVo == null) {
            return null;
        }
        holidaysTourVo.setCoverImageUrl(holidaysTourVo.getCoverImage());
        holidaysTourVo.setMapImageUrl(holidaysTourVo.getMapImage());
        holidaysTourVo.setCollectionTag(resolveCollectionTag(id));
        fillCountryNames(List.of(holidaysTourVo));
        return holidaysTourVo;
    }

    /**
     * 分页查询线路管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 线路管理分页列表
     */
    @Override
    public PageResult<HolidaysTourVo> queryPageList(HolidaysTourBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysTour> lqw = buildQueryWrapper(bo);
        Page<HolidaysTourVo> result = holidaysTourMapper.selectVoPage(pageQuery.build(), lqw);
        List<HolidaysTourVo> records = result.getRecords();
        records.forEach(t -> {
            t.setCoverImageUrl(t.getCoverImage());
            t.setMapImageUrl(t.getMapImage());
            // TODO 查询 holidays_tour_itinerary 表获取途径的国家
        });
        fillCollectionTags(records);
        fillCountryNames(records);
        return PageResult.build(records, result.getTotal());
    }

    /**
     * 查询符合条件的线路管理列表
     *
     * @param bo 查询条件
     * @return 线路管理列表
     */
    @Override
    public List<HolidaysTourVo> queryList(HolidaysTourBo bo) {
        LambdaQueryWrapper<HolidaysTour> lqw = buildQueryWrapper(bo);
        List<HolidaysTourVo> records = holidaysTourMapper.selectVoList(lqw);
        fillCollectionTags(records);
        fillCountryNames(records);
        return records;
    }


    private LambdaQueryWrapper<HolidaysTour> buildQueryWrapper(HolidaysTourBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HolidaysTour> lqw = QueryBuilder.lambda(HolidaysTour.class)
            .eqIfText(HolidaysTour::getCode, bo.getCode())
            .likeIfText(HolidaysTour::getName, bo.getName())
            .eqIfText(HolidaysTour::getDescription, bo.getDescription())
            .eqIfPresent(HolidaysTour::getDurationDays, bo.getDurationDays())
            .eqIfPresent(HolidaysTour::getTravelStyle, bo.getTravelStyle())
            .eqIfPresent(HolidaysTour::getServiceLevel, bo.getServiceLevel())
            .eqIfPresent(HolidaysTour::getPhysicalRating, bo.getPhysicalRating())
            .eqIfPresent(HolidaysTour::getTripType, bo.getTripType())
            .eqIfPresent(HolidaysTour::getMinAge, bo.getMinAge())
            .eqIfPresent(HolidaysTour::getBasePrice, bo.getBasePrice())
            .eqIfPresent(HolidaysTour::getSalePrice, bo.getSalePrice())
            .eqIfPresent(HolidaysTour::getSingleSupplement, bo.getSingleSupplement())
            .eqIfText(HolidaysTour::getCurrency, bo.getCurrency())
            .eqIfText(HolidaysTour::getCoverImage, bo.getCoverImage())
            .eqIfText(HolidaysTour::getMapImage, bo.getMapImage())
            .eqIfText(HolidaysTour::getNotes, bo.getNotes())
            .eqIfPresent(HolidaysTour::getStatus, bo.getStatus())
            .eqIfText(HolidaysTour::getSeoTitle, bo.getSeoTitle())
            .eqIfText(HolidaysTour::getSeoDescription, bo.getSeoDescription())
            .eqIfText(HolidaysTour::getSeoKeywords, bo.getSeoKeywords())
            .betweenParams(HolidaysTour::getDeletedAt, params, "beginDeletedAt", "endDeletedAt")
            .orderByAsc(HolidaysTour::getId)
            .build();
        applyCountryFilter(lqw, bo.getCountryName());
        return lqw;
    }

    /**
     * 新增线路管理
     *
     * @param bo 线路管理
     * @return 是否新增成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(HolidaysTourBo bo) {
        validateCollectionTag(bo.getCollectionTag());
        HolidaysTour add = MapstructUtils.convert(bo, HolidaysTour.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysTourMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            replaceCollectionTag(add.getId(), bo.getCollectionTag());
            publishTourSearchRebuild(add.getId());
        }
        return flag;
    }

    /**
     * 修改线路管理
     *
     * @param bo 线路管理
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(HolidaysTourBo bo) {
        validateCollectionTag(bo.getCollectionTag());
        HolidaysTour update = MapstructUtils.convert(bo, HolidaysTour.class);
        validEntityBeforeSave(update);
        boolean flag = holidaysTourMapper.updateById(update) > 0;
        if (flag) {
            replaceCollectionTag(bo.getId(), bo.getCollectionTag());
            publishTourSearchRebuild(bo.getId());
        }
        return flag;
    }

    /**
     * 修改线路管理状态
     *
     * @param id 主键
     * @param status 状态值
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Long status) {
        boolean flag = holidaysTourMapper.lambda()
            .set(HolidaysTour::getStatus, status)
            .eq(HolidaysTour::getId, id)
            .update();
        if (flag) {
            publishTourSearchRebuild(id);
        }
        return flag;
    }


    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysTour entity) {
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除线路管理信息
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
        holidaysTourTagRelMapper.lambda().in(HolidaysTourTagRel::getTourId, ids).delete();
        boolean flag = holidaysTourMapper.deleteByIds(ids) > 0;
        if (flag) {
            ids.forEach(this::publishTourSearchDelete);
        }
        return flag;
    }

    private void publishTourSearchRebuild(Long tourId) {
        if (tourId != null) {
            eventPublisher.publishEvent(TourSearchRebuildEvent.rebuild(tourId));
        }
    }

    private void publishTourSearchDelete(Long tourId) {
        if (tourId != null) {
            eventPublisher.publishEvent(TourSearchRebuildEvent.delete(tourId));
        }
    }

    private void validateCollectionTag(String collectionTag) {
        if (StringUtils.isBlank(collectionTag)) {
            return;
        }
        Long count = sysDictDataMapper.lambda()
            .eq(SysDictData::getDictType, COLLECTION_DICT_TYPE)
            .eq(SysDictData::getDictValue, collectionTag)
            .count();
        if (count == null || count == 0) {
            throw new ServiceException("CollectionTag不存在");
        }
    }

    private void replaceCollectionTag(Long tourId, String collectionTag) {
        holidaysTourTagRelMapper.lambda().eq(HolidaysTourTagRel::getTourId, tourId).delete();
        if (StringUtils.isBlank(collectionTag)) {
            return;
        }
        HolidaysTourTagRel rel = new HolidaysTourTagRel();
        rel.setTourId(tourId);
        rel.setDictCode(collectionTag);
        holidaysTourTagRelMapper.insert(rel);
    }

    private String resolveCollectionTag(Long tourId) {
        HolidaysTourTagRel rel = holidaysTourTagRelMapper.selectOne(Wrappers.lambdaQuery(HolidaysTourTagRel.class)
            .eq(HolidaysTourTagRel::getTourId, tourId)
            .orderByAsc(HolidaysTourTagRel::getId)
            .last("limit 1"));
        return rel == null ? null : rel.getDictCode();
    }

    private void fillCollectionTags(List<HolidaysTourVo> records) {
        List<Long> tourIds = records.stream()
            .map(HolidaysTourVo::getId)
            .filter(Objects::nonNull)
            .toList();
        if (tourIds.isEmpty()) {
            return;
        }
        List<HolidaysTourTagRel> rels = holidaysTourTagRelMapper.selectList(Wrappers.lambdaQuery(HolidaysTourTagRel.class)
            .in(HolidaysTourTagRel::getTourId, tourIds)
            .orderByAsc(HolidaysTourTagRel::getTourId)
            .orderByAsc(HolidaysTourTagRel::getId));
        Map<Long, String> collectionTagMap = rels.stream()
            .collect(Collectors.toMap(HolidaysTourTagRel::getTourId, HolidaysTourTagRel::getDictCode, (first, second) -> first));
        records.forEach(item -> item.setCollectionTag(collectionTagMap.get(item.getId())));
    }

    private void applyCountryFilter(LambdaQueryWrapper<HolidaysTour> lqw, String countryName) {
        if (StringUtils.isBlank(countryName)) {
            return;
        }
        List<Long> countryIds = destinationMapper.lambda()
            .select(HolidaysDestination::getId)
            .eq(HolidaysDestination::getLevel, DESTINATION_LEVEL_COUNTRY)
            .like(HolidaysDestination::getName, countryName)
            .list()
            .stream()
            .map(HolidaysDestination::getId)
            .toList();
        if (countryIds.isEmpty()) {
            lqw.eq(HolidaysTour::getId, -1L);
            return;
        }
        List<Integer> destinationIds = destinationMapper.lambda()
            .select(HolidaysDestination::getId)
            .and(w -> w.in(HolidaysDestination::getId, countryIds)
                .or()
                .in(HolidaysDestination::getParentId, countryIds))
            .list()
            .stream()
            .map(HolidaysDestination::getId)
            .map(Long::intValue)
            .toList();
        if (destinationIds.isEmpty()) {
            lqw.eq(HolidaysTour::getId, -1L);
            return;
        }
        List<Long> tourIds = itineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
                .select(HolidaysTourItinerary::getTourId)
                .and(w -> w.in(HolidaysTourItinerary::getFromDestinationId, destinationIds)
                    .or()
                    .in(HolidaysTourItinerary::getToDestinationId, destinationIds)))
            .stream()
            .map(HolidaysTourItinerary::getTourId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (tourIds.isEmpty()) {
            lqw.eq(HolidaysTour::getId, -1L);
            return;
        }
        lqw.in(HolidaysTour::getId, tourIds);
    }

    private void fillCountryNames(List<HolidaysTourVo> records) {
        List<Long> tourIds = records.stream()
            .map(HolidaysTourVo::getId)
            .filter(Objects::nonNull)
            .toList();
        if (tourIds.isEmpty()) {
            return;
        }
        List<HolidaysTourItinerary> itineraries = itineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
            .in(HolidaysTourItinerary::getTourId, tourIds)
            .orderByAsc(HolidaysTourItinerary::getTourId)
            .orderByAsc(HolidaysTourItinerary::getDayNumber)
            .orderByAsc(HolidaysTourItinerary::getId));
        Set<Long> destinationIds = itineraries.stream()
            .flatMap(item -> {
                List<Long> ids = new ArrayList<>(2);
                if (item.getFromDestinationId() != null) {
                    ids.add(item.getFromDestinationId().longValue());
                }
                if (item.getToDestinationId() != null) {
                    ids.add(item.getToDestinationId().longValue());
                }
                return ids.stream();
            })
            .collect(Collectors.toSet());
        if (destinationIds.isEmpty()) {
            return;
        }
        Map<Long, HolidaysDestination> destinationMap = destinationMapper.selectList(Wrappers.lambdaQuery(HolidaysDestination.class)
                .in(HolidaysDestination::getId, destinationIds))
            .stream()
            .collect(Collectors.toMap(HolidaysDestination::getId, item -> item));
        Set<Long> parentCountryIds = destinationMap.values().stream()
            .filter(item -> Objects.equals(item.getLevel(), DESTINATION_LEVEL_CITY))
            .map(HolidaysDestination::getParentId)
            .filter(Objects::nonNull)
            .filter(id -> !destinationMap.containsKey(id))
            .collect(Collectors.toSet());
        if (!parentCountryIds.isEmpty()) {
            destinationMapper.selectList(Wrappers.lambdaQuery(HolidaysDestination.class)
                    .in(HolidaysDestination::getId, parentCountryIds))
                .forEach(item -> destinationMap.put(item.getId(), item));
        }
        Map<Long, LinkedHashSet<String>> countryNameMap = new HashMap<>();
        itineraries.forEach(item -> {
            LinkedHashSet<String> names = countryNameMap.computeIfAbsent(item.getTourId(), key -> new LinkedHashSet<>());
            addCountryName(names, item.getFromDestinationId(), destinationMap);
            addCountryName(names, item.getToDestinationId(), destinationMap);
        });
        records.forEach(item -> {
            LinkedHashSet<String> names = countryNameMap.get(item.getId());
            item.setCountryNames(names == null ? List.of() : List.copyOf(names));
        });
    }

    private void addCountryName(LinkedHashSet<String> names, Integer destinationId, Map<Long, HolidaysDestination> destinationMap) {
        if (destinationId == null) {
            return;
        }
        HolidaysDestination destination = destinationMap.get(destinationId.longValue());
        if (destination == null) {
            return;
        }
        HolidaysDestination country = Objects.equals(destination.getLevel(), DESTINATION_LEVEL_COUNTRY)
            ? destination
            : destinationMap.get(destination.getParentId());
        if (country != null && StringUtils.isNotBlank(country.getName())) {
            names.add(country.getName());
        }
    }
}


