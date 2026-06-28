package com.boxhilltravel.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.HolidaysDestinationPageContent;
import com.boxhilltravel.core.domain.HolidaysDestinationTagRel;
import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import com.boxhilltravel.core.domain.vo.DestinationPageCityVo;
import com.boxhilltravel.core.domain.vo.DestinationPageHighlightVo;
import com.boxhilltravel.core.domain.vo.DestinationPagePracticalInfoVo;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.core.mapper.HolidaysDestinationPageContentMapper;
import com.boxhilltravel.core.mapper.HolidaysDestinationTagRelMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryMapper;
import com.boxhilltravel.core.mapper.HolidaysTourMapper;
import com.boxhilltravel.customer.domain.vo.DestinationPageContentVo;
import com.boxhilltravel.customer.domain.vo.DestinationVo;
import com.boxhilltravel.customer.domain.vo.PopularDestinationVo;
import com.boxhilltravel.customer.service.IHolidaysDestinationCustomerService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Customer destination service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysDestinationCustomerServiceImpl implements IHolidaysDestinationCustomerService {

    private static final String POPULAR_DESTINATION_DICT_VALUE = "1";
    private static final String FEATURED_DESTINATION_DICT_VALUE = "2";
    private static final Long PAGE_CONTENT_STATUS_PUBLISHED = 1L;

    private final HolidaysDestinationMapper destinationMapper;
    private final HolidaysDestinationPageContentMapper pageContentMapper;
    private final HolidaysTourItineraryMapper itineraryMapper;
    private final HolidaysDestinationTagRelMapper destinationTagRelMapper;
    private final HolidaysTourMapper tourMapper;

    @Override
    public List<DestinationVo> queryDestinationTree() {
        List<HolidaysDestination> list = queryActiveDestinations();
        return buildTree(list, 0L);
    }

    @Override
    public List<DestinationVo> queryChildrenByParentId(Long parentId) {
        return destinationMapper.selectList(Wrappers.lambdaQuery(HolidaysDestination.class)
                .eq(HolidaysDestination::getParentId, parentId)
                .eq(HolidaysDestination::getStatus, 1L)
                .isNull(HolidaysDestination::getDeletedAt)
                .orderByAsc(HolidaysDestination::getSort)
                .orderByAsc(HolidaysDestination::getId))
            .stream()
            .map(this::toDestinationVo)
            .toList();
    }

    @Override
    public List<DestinationVo> queryAvailableTree(String destinationNameEn) {
        Long parentId = 0L;
        if (StringUtils.isNotBlank(destinationNameEn)) {
            HolidaysDestination destination = selectByNameEn(destinationNameEn);
            if (destination != null) {
                parentId = destination.getId();
            }
        }

        Set<Long> cityIds = queryActiveTourDestinationIds();
        if (cityIds.isEmpty()) {
            return List.of();
        }
        List<HolidaysDestination> cities = destinationMapper.selectByIds(cityIds);
        Set<Long> countryIds = cities.stream()
            .map(HolidaysDestination::getParentId)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<HolidaysDestination> countries = countryIds.isEmpty() ? List.of() : destinationMapper.selectByIds(countryIds);
        Set<Long> continentIds = countries.stream()
            .map(HolidaysDestination::getParentId)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<HolidaysDestination> continents = continentIds.isEmpty() ? List.of() : destinationMapper.selectByIds(continentIds);

        List<HolidaysDestination> allNodes = new ArrayList<>();
        allNodes.addAll(continents);
        allNodes.addAll(countries);
        allNodes.addAll(cities);
        allNodes = allNodes.stream()
            .filter(this::isActiveDestination)
            .distinct()
            .sorted((a, b) -> {
                int sortCompare = Integer.compare(a.getSort() == null ? Integer.MAX_VALUE : a.getSort(), b.getSort() == null ? Integer.MAX_VALUE : b.getSort());
                return sortCompare != 0 ? sortCompare : Long.compare(a.getId(), b.getId());
            })
            .toList();
        return buildTree(allNodes, parentId);
    }

    @Override
    public DestinationVo queryDestination(String destinationNameEn) {
        HolidaysDestination destination = selectByNameEn(destinationNameEn);
        if (destination == null) {
            return null;
        }
        DestinationVo vo = toDestinationVo(destination);
        if (Long.valueOf(2L).equals(destination.getLevel())) {
            vo.setPageContent(queryPublishedPageContent(destination.getId()));
        }
        return vo;
    }

    @Override
    public List<PopularDestinationVo> queryPopularDestinations(Integer limit) {
        int resolvedLimit = limit == null || limit <= 0 ? 6 : Math.min(limit, 6);
        List<HolidaysDestinationTagRel> rels = destinationTagRelMapper.selectList(Wrappers.lambdaQuery(HolidaysDestinationTagRel.class)
            // HomeRecommend
            .eq(HolidaysDestinationTagRel::getDictValue, POPULAR_DESTINATION_DICT_VALUE)
            .orderByAsc(HolidaysDestinationTagRel::getSortOrder)
            .orderByAsc(HolidaysDestinationTagRel::getId));
        List<PopularDestinationVo> result = new ArrayList<>();
        for (HolidaysDestinationTagRel rel : rels) {
            if (result.size() >= resolvedLimit) {
                break;
            }
            HolidaysDestination country = destinationMapper.selectById(rel.getDestinationId());
            if (!isPopularCountry(country)) {
                continue;
            }
            HolidaysDestination continent = destinationMapper.selectById(country.getParentId());
            if (!isValidContinent(continent)) {
                continue;
            }
            PopularDestinationVo vo = new PopularDestinationVo();
            vo.setId(country.getId());
            vo.setName(country.getName());
            vo.setNameEn(country.getNameEn());
            vo.setImage(country.getImage());
            vo.setDescription(country.getDescription());
            vo.setContinentName(continent.getName());
            vo.setContinentNameEn(continent.getNameEn());
            fillLowestPriceTour(vo, country.getId());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<DestinationVo> queryFeaturedDestinations() {
        List<HolidaysDestinationTagRel> rels = destinationTagRelMapper.selectList(Wrappers.lambdaQuery(HolidaysDestinationTagRel.class)
            .eq(HolidaysDestinationTagRel::getDictValue, FEATURED_DESTINATION_DICT_VALUE)
            .orderByAsc(HolidaysDestinationTagRel::getSortOrder)
            .orderByAsc(HolidaysDestinationTagRel::getId));
        List<DestinationVo> result = new ArrayList<>();
        Set<Long> destinationIds = new LinkedHashSet<>();
        for (HolidaysDestinationTagRel rel : rels) {
            if (!destinationIds.add(rel.getDestinationId())) {
                continue;
            }
            HolidaysDestination destination = destinationMapper.selectById(rel.getDestinationId());
            if (!isActiveDestination(destination)) {
                continue;
            }
            result.add(toDestinationVo(destination));
        }
        return result;
    }

    @Override
    public Map<Long, Long> queryTourCounts(List<Long> countryIds) {
        if (countryIds == null || countryIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> resolvedCountryIds = countryIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (resolvedCountryIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = resolvedCountryIds.stream()
            .collect(Collectors.toMap(id -> id, id -> 0L, (a, b) -> a, LinkedHashMap::new));
        List<HolidaysDestination> countries = destinationMapper.selectList(Wrappers.lambdaQuery(HolidaysDestination.class)
            .in(HolidaysDestination::getId, resolvedCountryIds)
            .eq(HolidaysDestination::getLevel, 2L)
            .eq(HolidaysDestination::getStatus, 1L)
            .isNull(HolidaysDestination::getDeletedAt));

        Map<Long, Set<Long>> destinationCountryMap = new LinkedHashMap<>();
        for (HolidaysDestination country : countries) {
            Set<Long> destinationIds = collectActiveDestinationIds(country.getId());
            for (Long destinationId : destinationIds) {
                destinationCountryMap.computeIfAbsent(destinationId, key -> new LinkedHashSet<>()).add(country.getId());
            }
        }
        if (destinationCountryMap.isEmpty()) {
            return result;
        }

        List<HolidaysTourItinerary> itineraries = itineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
            .select(HolidaysTourItinerary::getTourId, HolidaysTourItinerary::getFromDestinationId, HolidaysTourItinerary::getToDestinationId)
            .and(wrapper -> wrapper
                .in(HolidaysTourItinerary::getFromDestinationId, destinationCountryMap.keySet())
                .or()
                .in(HolidaysTourItinerary::getToDestinationId, destinationCountryMap.keySet()))
            .exists("select 1 from holidays_tour t where t.id = holidays_tour_itinerary.tour_id and t.status = 1 and t.deleted_at is null"));
        Map<Long, Set<Long>> countryTourMap = new LinkedHashMap<>();
        for (HolidaysTourItinerary itinerary : itineraries) {
            if (itinerary == null || itinerary.getTourId() == null) {
                continue;
            }
            collectCountryTour(countryTourMap, destinationCountryMap, itinerary.getFromDestinationId(), itinerary.getTourId());
            collectCountryTour(countryTourMap, destinationCountryMap, itinerary.getToDestinationId(), itinerary.getTourId());
        }
        countryTourMap.forEach((countryId, tourIds) -> result.put(countryId, (long) tourIds.size()));
        return result;
    }

    private void fillLowestPriceTour(PopularDestinationVo vo, Long destinationId) {
        Set<Long> destinationIds = collectActiveDestinationIds(destinationId);
        if (destinationIds.isEmpty()) {
            return;
        }
        HolidaysTour tour = tourMapper.selectLowestPriceTourByDestinationIds(new ArrayList<>(destinationIds));
        if (tour == null) {
            return;
        }
        vo.setPrice(tour.getSalePrice() == null ? tour.getBasePrice() : tour.getSalePrice());
        vo.setDuration(tour.getDurationDays());
        vo.setDurationDays(tour.getDurationDays());
        vo.setCurrency(tour.getCurrency());
    }

    private Set<Long> collectActiveDestinationIds(Long destinationId) {
        Set<Long> destinationIds = new LinkedHashSet<>();
        collectActiveDestinationIds(destinationId, destinationIds);
        return destinationIds;
    }

    private void collectActiveDestinationIds(Long destinationId, Set<Long> destinationIds) {
        if (destinationId == null || !destinationIds.add(destinationId)) {
            return;
        }
        List<HolidaysDestination> children = destinationMapper.selectList(Wrappers.lambdaQuery(HolidaysDestination.class)
            .eq(HolidaysDestination::getParentId, destinationId)
            .eq(HolidaysDestination::getStatus, 1L)
            .isNull(HolidaysDestination::getDeletedAt));
        for (HolidaysDestination child : children) {
            collectActiveDestinationIds(child.getId(), destinationIds);
        }
    }

    private void collectCountryTour(Map<Long, Set<Long>> countryTourMap, Map<Long, Set<Long>> destinationCountryMap,
                                    Integer destinationId, Long tourId) {
        if (destinationId == null || tourId == null) {
            return;
        }
        Set<Long> countryIds = destinationCountryMap.get(destinationId.longValue());
        if (countryIds == null || countryIds.isEmpty()) {
            return;
        }
        for (Long countryId : countryIds) {
            countryTourMap.computeIfAbsent(countryId, key -> new LinkedHashSet<>()).add(tourId);
        }
    }

    private List<HolidaysDestination> queryActiveDestinations() {
        return destinationMapper.selectList(Wrappers.lambdaQuery(HolidaysDestination.class)
            .eq(HolidaysDestination::getStatus, 1L)
            .isNull(HolidaysDestination::getDeletedAt)
            .orderByAsc(HolidaysDestination::getSort)
            .orderByAsc(HolidaysDestination::getId));
    }

    private Set<Long> queryActiveTourDestinationIds() {
        List<HolidaysTourItinerary> itineraries = itineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
            .select(HolidaysTourItinerary::getFromDestinationId, HolidaysTourItinerary::getToDestinationId)
            .exists("select 1 from holidays_tour t where t.id = holidays_tour_itinerary.tour_id and t.status = 1 and t.deleted_at is null"));
        Set<Long> destinationIds = new LinkedHashSet<>();
        for (HolidaysTourItinerary itinerary : itineraries) {
            if (Objects.isNull(itinerary)) {
                continue;
            }
            if (itinerary.getFromDestinationId() != null) {
                destinationIds.add(itinerary.getFromDestinationId().longValue());
            }
            if (itinerary.getToDestinationId() != null) {
                destinationIds.add(itinerary.getToDestinationId().longValue());
            }
        }
        return destinationIds;
    }

    private HolidaysDestination selectByNameEn(String destinationNameEn) {
        if (StringUtils.isBlank(destinationNameEn)) {
            return null;
        }
        return destinationMapper.selectOne(Wrappers.lambdaQuery(HolidaysDestination.class)
            .eq(HolidaysDestination::getNameEn, destinationNameEn)
            .eq(HolidaysDestination::getStatus, 1L)
            .isNull(HolidaysDestination::getDeletedAt)
            .last("limit 1"));
    }

    private List<DestinationVo> buildTree(List<HolidaysDestination> list, Long parentId) {
        List<DestinationVo> returnList = new ArrayList<>();
        for (Iterator<HolidaysDestination> iterator = list.iterator(); iterator.hasNext(); ) {
            HolidaysDestination destination = iterator.next();
            if (destination.getParentId() != null && destination.getParentId().longValue() == parentId.longValue()) {
                DestinationVo node = toDestinationVo(destination);
                recursionFn(list, node);
                returnList.add(node);
            }
        }
        return returnList;
    }

    private void recursionFn(List<HolidaysDestination> list, DestinationVo node) {
        List<DestinationVo> children = getChildList(list, node);
        node.setChildren(children);
        for (DestinationVo child : children) {
            if (!getChildList(list, child).isEmpty()) {
                recursionFn(list, child);
            }
        }
    }

    private List<DestinationVo> getChildList(List<HolidaysDestination> list, DestinationVo node) {
        List<DestinationVo> children = new ArrayList<>();
        for (HolidaysDestination destination : list) {
            if (destination.getParentId() != null && destination.getParentId().longValue() == node.getId().longValue()) {
                children.add(toDestinationVo(destination));
            }
        }
        return children;
    }

    private DestinationVo toDestinationVo(HolidaysDestination destination) {
        DestinationVo vo = new DestinationVo();
        vo.setId(destination.getId());
        vo.setName(destination.getName());
        vo.setNameEn(destination.getNameEn());
        vo.setParentId(destination.getParentId());
        vo.setLevel(destination.getLevel() == null ? null : destination.getLevel().intValue());
        vo.setImage(destination.getImage());
        vo.setDescription(destination.getDescription());
        vo.setSort(destination.getSort());
        return vo;
    }

    private DestinationPageContentVo queryPublishedPageContent(Long destinationId) {
        HolidaysDestinationPageContent content = pageContentMapper.selectOne(Wrappers.lambdaQuery(HolidaysDestinationPageContent.class)
            .eq(HolidaysDestinationPageContent::getDestinationId, destinationId)
            .eq(HolidaysDestinationPageContent::getStatus, PAGE_CONTENT_STATUS_PUBLISHED)
            .last("limit 1"));
        if (content == null) {
            return null;
        }
        DestinationPageContentVo vo = new DestinationPageContentVo();
        vo.setCurrencyDictValue(content.getCurrencyDictValue());
        vo.setLanguageDictValue(content.getLanguageDictValue());
        vo.setTimeZoneDictValue(content.getTimeZoneDictValue());
        vo.setVisa(content.getVisa());
        vo.setVisaTitle(content.getVisaTitle());
        vo.setVisaNote(content.getVisaNote());
        vo.setIntroduction(parseArray(content.getIntroductionJson(), String.class));
        vo.setHighlights(parseArray(content.getHighlightsJson(), DestinationPageHighlightVo.class));
        vo.setCities(parseArray(content.getCitiesJson(), DestinationPageCityVo.class));
        vo.setPracticalInfo(parseArray(content.getPracticalInfoJson(), DestinationPagePracticalInfoVo.class));
        return vo;
    }

    private <T> List<T> parseArray(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return List.of();
        }
        return JsonUtils.parseArray(json, clazz);
    }

    private boolean isPopularCountry(HolidaysDestination destination) {
        return isActiveDestination(destination)
            && Long.valueOf(2L).equals(destination.getLevel())
            && StringUtils.isNotBlank(destination.getNameEn());
    }

    private boolean isValidContinent(HolidaysDestination destination) {
        return isActiveDestination(destination)
            && Long.valueOf(1L).equals(destination.getLevel())
            && StringUtils.isNotBlank(destination.getNameEn());
    }

    private boolean isActiveDestination(HolidaysDestination destination) {
        return destination != null
            && Long.valueOf(1L).equals(destination.getStatus())
            && destination.getDeletedAt() == null;
    }

}
