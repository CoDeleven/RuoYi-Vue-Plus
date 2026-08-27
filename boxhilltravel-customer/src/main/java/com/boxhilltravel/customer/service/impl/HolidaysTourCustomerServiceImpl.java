package com.boxhilltravel.customer.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import com.boxhilltravel.core.domain.HolidaysTourItineraryActivity;
import com.boxhilltravel.core.domain.HolidaysTourServiceItem;
import com.boxhilltravel.core.domain.HolidaysTourTagRel;
import com.boxhilltravel.core.domain.query.CustomerTourQuery;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.core.mapper.HolidaysFeaturedTourMapper;
import com.boxhilltravel.core.mapper.HolidaysHotDealTourMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryActivityMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryMapper;
import com.boxhilltravel.core.mapper.HolidaysTourMapper;
import com.boxhilltravel.core.mapper.HolidaysTourServiceItemMapper;
import com.boxhilltravel.core.mapper.HolidaysTourTagRelMapper;
import com.boxhilltravel.customer.domain.bo.QueryTourBo;
import com.boxhilltravel.customer.domain.vo.ActivityItemVo;
import com.boxhilltravel.customer.domain.vo.DestinationVo;
import com.boxhilltravel.customer.domain.vo.FeaturedTourVo;
import com.boxhilltravel.customer.domain.vo.HotDealTourVo;
import com.boxhilltravel.customer.domain.vo.ItineraryItemVo;
import com.boxhilltravel.customer.domain.vo.TourCardVo;
import com.boxhilltravel.customer.domain.vo.TourDetailVo;
import com.boxhilltravel.customer.domain.vo.TourListItemVo;
import com.boxhilltravel.customer.service.IHolidaysTourCustomerService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Customer tour service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysTourCustomerServiceImpl implements IHolidaysTourCustomerService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "duration_days", "sale_price");
    private static final Set<String> ALLOWED_SORT_DIRECTIONS = Set.of("asc", "desc");

    private final HolidaysTourMapper tourMapper;
    private final HolidaysDestinationMapper destinationMapper;
    private final HolidaysTourItineraryMapper itineraryMapper;
    private final HolidaysTourItineraryActivityMapper activityMapper;
    private final HolidaysTourServiceItemMapper serviceItemMapper;
    private final HolidaysTourTagRelMapper tourTagRelMapper;
    private final HolidaysHotDealTourMapper hotDealTourMapper;
    private final HolidaysFeaturedTourMapper featuredTourMapper;
    private final HolidaysDepartureMapper departureMapper;

    @Override
    public PageResult<TourListItemVo> queryPageList(QueryTourBo bo) {
        QueryTourBo queryBo = bo == null ? new QueryTourBo() : bo;
        CustomerTourQuery query = buildCustomerTourQuery(queryBo);
        int pageNum = queryBo.getPageNum() == null || queryBo.getPageNum() <= 0 ? 1 : queryBo.getPageNum();
        int pageSize = queryBo.getPageSize() == null || queryBo.getPageSize() <= 0 ? 10 : queryBo.getPageSize();
        Page<HolidaysTour> page = tourMapper.selectCustomerTourPage(new Page<>(pageNum, pageSize), query);
        List<TourListItemVo> rows = page.getRecords().stream().map(this::toListItemVo).toList();
        return PageResult.build(rows, page.getTotal());
    }

    @Override
    public TourDetailVo queryDetail(Long id) {
        return buildDetail(tourMapper.selectById(id));
    }

    @Override
    public TourDetailVo queryDetailByCode(String code) {
        String normalizedCode = StringUtils.trim(code);
        if (StringUtils.isBlank(normalizedCode) || normalizedCode.length() > 64) {
            return null;
        }
        HolidaysTour tour = tourMapper.selectOne(Wrappers.lambdaQuery(HolidaysTour.class)
            .eq(HolidaysTour::getCode, normalizedCode));
        return buildDetail(tour);
    }

    private TourDetailVo buildDetail(HolidaysTour tour) {
        if (!isActiveTour(tour)) {
            return null;
        }
        Long id = tour.getId();
        TourDetailVo vo = toDetailVo(tour);
        List<HolidaysTourItinerary> itineraries = queryItineraries(id);
        List<HolidaysTourItineraryActivity> activities = activityMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItineraryActivity.class)
            .eq(HolidaysTourItineraryActivity::getTourId, id)
            .orderByAsc(HolidaysTourItineraryActivity::getSortOrder)
            .orderByAsc(HolidaysTourItineraryActivity::getId));
        Map<Long, List<ActivityItemVo>> activityMap = activities.stream()
            .collect(Collectors.groupingBy(HolidaysTourItineraryActivity::getItineraryId,
                Collectors.mapping(this::toActivityItemVo, Collectors.toList())));
        vo.setItineraryList(itineraries.stream().map(itinerary -> {
            ItineraryItemVo item = toItineraryItemVo(itinerary);
            item.setActivities(activityMap.getOrDefault(itinerary.getId(), List.of()));
            return item;
        }).toList());
        return vo;
    }

    @Override
    public List<HotDealTourVo> queryHotTours(Integer limit) {
        return hotDealTourMapper.selectHotToursForCustomer(resolveLimit(limit)).stream()
            .map(tour -> {
                HotDealTourVo vo = new HotDealTourVo();
                fillTourCard(vo, tour);
                return vo;
            })
            .toList();
    }

    @Override
    public List<FeaturedTourVo> queryFeaturedTours(Integer limit) {
        return featuredTourMapper.selectFeaturedToursForCustomer(resolveLimit(limit)).stream()
            .map(tour -> {
                FeaturedTourVo vo = new FeaturedTourVo();
                fillTourCard(vo, tour);
                return vo;
            })
            .toList();
    }

    private CustomerTourQuery buildCustomerTourQuery(QueryTourBo bo) {
        CustomerTourQuery query = new CustomerTourQuery();
        query.setTravelStyleList(bo.getTravelStyleList());
        query.setCollectionList(bo.getCollectionList());
        applyDurationRange(query, bo.getDurationList());
        if (bo.getDestinationId() != null) {
            query.setDestinationIds(new ArrayList<>(collectDestinationIds(bo.getDestinationId())));
        }
        if (StringUtils.isNotBlank(bo.getKeyword())) {
            query.setKeyword(StringUtils.trim(bo.getKeyword()));
        }
        if (StringUtils.isNotBlank(bo.getSortField())) {
            String sortField = StringUtils.toUnderScoreCase(bo.getSortField()).toLowerCase();
            if (ALLOWED_SORT_FIELDS.contains(sortField)) {
                query.setSortField(sortField);
            }
        }
        if (StringUtils.isNotBlank(bo.getSortDirection())) {
            String sortDirection = bo.getSortDirection().toLowerCase();
            if (ALLOWED_SORT_DIRECTIONS.contains(sortDirection)) {
                query.setSortDirection(sortDirection);
            }
        }
        return query;
    }

    private void applyDurationRange(CustomerTourQuery query, List<Integer> durationList) {
        if (durationList == null || durationList.isEmpty()) {
            return;
        }
        Integer min = CollectionUtil.getFirst(durationList);
        Integer max = CollectionUtil.getLast(durationList);

        if (Objects.isNull(min) || Objects.isNull(max)) {
            return;
        }

        query.setMinDurationDays(min);
        query.setMaxDurationDays(max);
    }

    private TourListItemVo toListItemVo(HolidaysTour tour) {
        TourListItemVo vo = new TourListItemVo();
        fillTourCard(vo, tour);
        vo.setMapImage(tour.getMapImage());
        vo.setMinAge(toInteger(tour.getMinAge()));
        vo.setServiceLevel(toInteger(tour.getServiceLevel()));
        vo.setPhysicalRating(toInteger(tour.getPhysicalRating()));
        vo.setStatus(toInteger(tour.getStatus()));
        vo.setVersion(tour.getVersion() == null ? null : tour.getVersion().longValue());
        vo.setTravelCollection(resolveFirstCollection(tour.getId()));
        return vo;
    }

    private TourDetailVo toDetailVo(HolidaysTour tour) {
        TourDetailVo vo = new TourDetailVo();
        vo.setId(tour.getId());
        vo.setCode(tour.getCode());
        vo.setName(tour.getName());
        vo.setDescription(tour.getDescription());
        vo.setDurationDays(tour.getDurationDays());
        vo.setTravelStyle(toInteger(tour.getTravelStyle()));
        vo.setServiceLevel(toInteger(tour.getServiceLevel()));
        vo.setPhysicalRating(toInteger(tour.getPhysicalRating()));
        vo.setBasePrice(tour.getBasePrice());
        vo.setSalePrice(tour.getSalePrice());
        vo.setSingleSupplement(tour.getSingleSupplement());
        vo.setCurrency(tour.getCurrency());
        vo.setCoverImage(tour.getCoverImage());
        vo.setMapImage(tour.getMapImage());
        vo.setNotes(tour.getNotes());
        vo.setLatestArrivalTime(tour.getLatestArrivalTime());
        vo.setEarliestDepartureTime(tour.getEarliestDepartureTime());
        vo.setMinAge(toInteger(tour.getMinAge()));
        vo.setStatus(toInteger(tour.getStatus()));
        vo.setVersion(tour.getVersion() == null ? null : tour.getVersion().longValue());
        vo.setTripType(toInteger(tour.getTripType()));
        vo.setDestinations(queryDestinationsByTourId(tour.getId()).stream().map(DestinationVo::getName).toList());
        vo.setTravelCollection(resolveFirstCollection(tour.getId()));
        vo.setUpcomingDepartureDate(resolveUpcomingDepartureDate(tour.getId()));
        fillServiceItems(vo, tour.getId());
        return vo;
    }

    private void fillTourCard(TourCardVo vo, HolidaysTour tour) {
        vo.setId(tour.getId());
        vo.setCode(tour.getCode());
        vo.setName(tour.getName());
        vo.setDescription(tour.getDescription());
        vo.setCoverImage(tour.getCoverImage());
        vo.setDurationDays(tour.getDurationDays());
        vo.setTravelStyle(toInteger(tour.getTravelStyle()));
        vo.setBasePrice(tour.getBasePrice());
        vo.setSalePrice(tour.getSalePrice());
        vo.setSingleSupplement(tour.getSingleSupplement());
        vo.setCurrency(tour.getCurrency());
        vo.setTripType(toInteger(tour.getTripType()));
        vo.setUpcomingDepartureDate(resolveUpcomingDepartureDate(tour.getId()));
        vo.setDestinations(queryDestinationsByTourId(tour.getId()).stream().map(DestinationVo::getName).toList());
    }

    private void fillServiceItems(TourDetailVo vo, Long tourId) {
        List<HolidaysTourServiceItem> items = serviceItemMapper.selectList(Wrappers.lambdaQuery(HolidaysTourServiceItem.class)
            .eq(HolidaysTourServiceItem::getTourId, tourId)
            .orderByAsc(HolidaysTourServiceItem::getSortOrder)
            .orderByAsc(HolidaysTourServiceItem::getId));
        List<String> included = new ArrayList<>();
        List<String> excluded = new ArrayList<>();
        for (HolidaysTourServiceItem item : items) {
            if (StringUtils.isBlank(item.getContent())) {
                continue;
            }
            if (Long.valueOf(1L).equals(item.getItemType())) {
                included.add(item.getContent());
            } else if (Long.valueOf(2L).equals(item.getItemType())) {
                excluded.add(item.getContent());
            }
        }
        vo.setIncludedItems(included);
        vo.setExcludedItems(excluded);
    }

    private List<HolidaysTourItinerary> queryItineraries(Long tourId) {
        return itineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
            .eq(HolidaysTourItinerary::getTourId, tourId)
            .orderByAsc(HolidaysTourItinerary::getDayNumber)
            .orderByAsc(HolidaysTourItinerary::getId));
    }

    private ItineraryItemVo toItineraryItemVo(HolidaysTourItinerary itinerary) {
        ItineraryItemVo vo = new ItineraryItemVo();
        vo.setDayNo(itinerary.getDayNumber());
        vo.setTitle(itinerary.getTitle());
        vo.setDescription(itinerary.getDescription());
        vo.setMeals(StringUtils.isBlank(itinerary.getMeals()) ? List.of() : JsonUtils.parseArray(itinerary.getMeals(), String.class));
        vo.setHighlights(List.of());
        vo.setAccommodation("");
        return vo;
    }

    private ActivityItemVo toActivityItemVo(HolidaysTourItineraryActivity activity) {
        ActivityItemVo vo = new ActivityItemVo();
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        vo.setShowInPreview(Long.valueOf(1L).equals(activity.getShowInPreview()));
        if (StringUtils.isNotBlank(activity.getSubtitle())) {
            vo.setSubtitle(JsonUtils.parseArray(activity.getSubtitle(), String.class));
        }
        if (StringUtils.isNotBlank(activity.getActivityIcon())) {
            vo.setIconPath(activity.getActivityIcon());
        }
        return vo;
    }

    private List<DestinationVo> queryDestinationsByTourId(Long tourId) {
        List<HolidaysTourItinerary> itineraries = queryItineraries(tourId);
        itineraries.sort(Comparator.comparing(HolidaysTourItinerary::getDayNumber, Comparator.nullsLast(Comparator.naturalOrder())));
        LinkedHashSet<Long> destinationIds = new LinkedHashSet<>();
        for (HolidaysTourItinerary itinerary : itineraries) {
            if (itinerary.getFromDestinationId() != null) {
                destinationIds.add(itinerary.getFromDestinationId().longValue());
            }
            if (itinerary.getToDestinationId() != null) {
                destinationIds.add(itinerary.getToDestinationId().longValue());
            }
        }
        if (destinationIds.isEmpty()) {
            return List.of();
        }
        Map<Long, HolidaysDestination> destinationMap = destinationMapper.selectByIds(destinationIds).stream()
            .collect(Collectors.toMap(HolidaysDestination::getId, destination -> destination, (a, b) -> a));
        List<DestinationVo> result = new ArrayList<>();
        int sort = 1;
        for (Long destinationId : destinationIds) {
            HolidaysDestination destination = destinationMap.get(destinationId);
            if (destination != null) {
                DestinationVo vo = toDestinationVo(destination);
                vo.setSort(sort);
                result.add(vo);
            }
            sort++;
        }
        return result;
    }

    private DestinationVo toDestinationVo(HolidaysDestination destination) {
        DestinationVo vo = new DestinationVo();
        vo.setId(destination.getId());
        vo.setName(destination.getName());
        vo.setNameEn(destination.getNameEn());
        vo.setParentId(destination.getParentId());
        vo.setLevel(toInteger(destination.getLevel()));
        vo.setImage(destination.getImage());
        vo.setDescription(destination.getDescription());
        vo.setSort(destination.getSort());
        return vo;
    }

    private Integer resolveFirstCollection(Long tourId) {
        HolidaysTourTagRel rel = tourTagRelMapper.selectOne(Wrappers.lambdaQuery(HolidaysTourTagRel.class)
            .eq(HolidaysTourTagRel::getTourId, tourId)
            .orderByAsc(HolidaysTourTagRel::getId)
            .last("limit 1"));
        if (rel == null || StringUtils.isBlank(rel.getDictCode())) {
            return null;
        }
        try {
            return Integer.valueOf(rel.getDictCode());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String resolveUpcomingDepartureDate(Long tourId) {
        HolidaysDeparture departure = departureMapper.selectOne(Wrappers.lambdaQuery(HolidaysDeparture.class)
            .eq(HolidaysDeparture::getTourId, tourId)
            .ge(HolidaysDeparture::getDepartureDate, java.time.LocalDate.now())
            .in(HolidaysDeparture::getStatus, List.of(1, 2))
            .orderByAsc(HolidaysDeparture::getDepartureDate)
            .orderByAsc(HolidaysDeparture::getId)
            .last("limit 1"));
        return departure == null || departure.getDepartureDate() == null ? null : departure.getDepartureDate().format(DATE_FORMATTER);
    }

    private Set<Long> collectDestinationIds(Long destinationId) {
        Set<Long> destinationIds = new LinkedHashSet<>();
        collectDestinationIds(destinationId, destinationIds);
        return destinationIds;
    }

    private void collectDestinationIds(Long destinationId, Set<Long> destinationIds) {
        if (destinationId == null || !destinationIds.add(destinationId)) {
            return;
        }
        List<HolidaysDestination> children = destinationMapper.selectList(Wrappers.lambdaQuery(HolidaysDestination.class)
            .eq(HolidaysDestination::getParentId, destinationId)
            .eq(HolidaysDestination::getStatus, 1L)
            .isNull(HolidaysDestination::getDeletedAt));
        for (HolidaysDestination child : children) {
            collectDestinationIds(child.getId(), destinationIds);
        }
    }

    private boolean isActiveTour(HolidaysTour tour) {
        return tour != null && Long.valueOf(1L).equals(tour.getStatus()) && tour.getDeletedAt() == null;
    }

    private Integer resolveLimit(Integer limit) {
        return limit == null || limit <= 0 ? 6 : Math.min(limit, 20);
    }

    private Integer toInteger(Long value) {
        return value == null ? null : value.intValue();
    }

    private record DurationRange(Integer min, Integer max) {

        private static DurationRange of(Integer code) {
            return switch (code == null ? 0 : code) {
                case 1 -> new DurationRange(1, 3);
                case 2 -> new DurationRange(4, 6);
                case 3 -> new DurationRange(7, 9);
                case 4 -> new DurationRange(8, 13);
                case 5 -> new DurationRange(14, 21);
                case 6 -> new DurationRange(22, 28);
                case 7 -> new DurationRange(29, Integer.MAX_VALUE);
                default -> new DurationRange(null, null);
            };
        }

        private boolean isValid() {
            return min != null && max != null;
        }
    }

}
