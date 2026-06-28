package com.boxhilltravel.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysCustomerWishlist;
import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import com.boxhilltravel.core.domain.HolidaysTourTagRel;
import com.boxhilltravel.core.mapper.HolidaysCustomerWishlistMapper;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryMapper;
import com.boxhilltravel.core.mapper.HolidaysTourMapper;
import com.boxhilltravel.core.mapper.HolidaysTourTagRelMapper;
import com.boxhilltravel.customer.domain.vo.TourListItemVo;
import com.boxhilltravel.customer.service.ICustomerWishlistService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Customer wishlist service implementation.
 */
@RequiredArgsConstructor
@Service
public class CustomerWishlistServiceImpl implements ICustomerWishlistService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long DESTINATION_LEVEL_CITY = 3L;

    private final HolidaysCustomerWishlistMapper wishlistMapper;
    private final HolidaysTourMapper tourMapper;
    private final HolidaysTourItineraryMapper itineraryMapper;
    private final HolidaysDestinationMapper destinationMapper;
    private final HolidaysTourTagRelMapper tourTagRelMapper;
    private final HolidaysDepartureMapper departureMapper;

    @Override
    public List<TourListItemVo> list() {
        Long userId = LoginHelper.getUserId();
        List<HolidaysCustomerWishlist> wishlist = wishlistMapper.selectList(Wrappers.lambdaQuery(HolidaysCustomerWishlist.class)
            .eq(HolidaysCustomerWishlist::getCustomerUserId, userId)
            .orderByDesc(HolidaysCustomerWishlist::getCreateTime)
            .orderByDesc(HolidaysCustomerWishlist::getId));
        if (wishlist.isEmpty()) {
            return List.of();
        }
        List<Long> tourIds = wishlist.stream()
            .map(HolidaysCustomerWishlist::getTourId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, HolidaysTour> tourMap = tourMapper.selectList(Wrappers.lambdaQuery(HolidaysTour.class)
                .in(HolidaysTour::getId, tourIds)
                .eq(HolidaysTour::getStatus, 1L)
                .isNull(HolidaysTour::getDeletedAt))
            .stream()
            .collect(Collectors.toMap(HolidaysTour::getId, item -> item));
        return wishlist.stream()
            .map(item -> tourMap.get(item.getTourId()))
            .filter(Objects::nonNull)
            .map(this::toListItemVo)
            .toList();
    }

    @Override
    public List<Long> ids() {
        Long userId = LoginHelper.getUserId();
        return wishlistMapper.selectList(Wrappers.lambdaQuery(HolidaysCustomerWishlist.class)
                .select(HolidaysCustomerWishlist::getTourId)
                .eq(HolidaysCustomerWishlist::getCustomerUserId, userId)
                .orderByDesc(HolidaysCustomerWishlist::getCreateTime)
                .orderByDesc(HolidaysCustomerWishlist::getId))
            .stream()
            .map(HolidaysCustomerWishlist::getTourId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    @Override
    public Boolean status(Long tourId) {
        return exists(LoginHelper.getUserId(), tourId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long tourId) {
        Long userId = LoginHelper.getUserId();
        validateActiveTour(tourId);
        if (exists(userId, tourId)) {
            return;
        }
        HolidaysCustomerWishlist wishlist = new HolidaysCustomerWishlist();
        wishlist.setCustomerUserId(userId);
        wishlist.setTourId(tourId);
        wishlistMapper.insert(wishlist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long tourId) {
        wishlistMapper.delete(Wrappers.lambdaQuery(HolidaysCustomerWishlist.class)
            .eq(HolidaysCustomerWishlist::getCustomerUserId, LoginHelper.getUserId())
            .eq(HolidaysCustomerWishlist::getTourId, tourId));
    }

    private boolean exists(Long userId, Long tourId) {
        Long count = wishlistMapper.selectCount(Wrappers.lambdaQuery(HolidaysCustomerWishlist.class)
            .eq(HolidaysCustomerWishlist::getCustomerUserId, userId)
            .eq(HolidaysCustomerWishlist::getTourId, tourId));
        return count != null && count > 0;
    }

    private void validateActiveTour(Long tourId) {
        HolidaysTour tour = tourMapper.selectById(tourId);
        if (tour == null || !Long.valueOf(1L).equals(tour.getStatus()) || tour.getDeletedAt() != null) {
            throw new ServiceException("Tour not found");
        }
    }

    private TourListItemVo toListItemVo(HolidaysTour tour) {
        TourListItemVo vo = new TourListItemVo();
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
        vo.setDestinations(queryDestinationNamesByTourId(tour.getId()));
        vo.setMinAge(toInteger(tour.getMinAge()));
        vo.setServiceLevel(toInteger(tour.getServiceLevel()));
        vo.setPhysicalRating(toInteger(tour.getPhysicalRating()));
        vo.setStatus(toInteger(tour.getStatus()));
        vo.setVersion(tour.getVersion() == null ? null : tour.getVersion().longValue());
        vo.setTravelCollection(resolveFirstCollection(tour.getId()));
        return vo;
    }

    private List<String> queryDestinationNamesByTourId(Long tourId) {
        List<HolidaysTourItinerary> itineraries = itineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
            .eq(HolidaysTourItinerary::getTourId, tourId)
            .orderByAsc(HolidaysTourItinerary::getDayNumber)
            .orderByAsc(HolidaysTourItinerary::getId));
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
        List<Long> parentCountryIds = destinationMap.values().stream()
            .filter(item -> Objects.equals(item.getLevel(), DESTINATION_LEVEL_CITY))
            .map(HolidaysDestination::getParentId)
            .filter(Objects::nonNull)
            .filter(id -> !destinationMap.containsKey(id))
            .distinct()
            .toList();
        if (!parentCountryIds.isEmpty()) {
            destinationMapper.selectByIds(parentCountryIds).forEach(item -> destinationMap.put(item.getId(), item));
        }
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (Long destinationId : destinationIds) {
            HolidaysDestination destination = destinationMap.get(destinationId);
            if (destination == null) {
                continue;
            }
            HolidaysDestination display = Objects.equals(destination.getLevel(), DESTINATION_LEVEL_CITY)
                ? destinationMap.get(destination.getParentId())
                : destination;
            if (display != null && display.getName() != null) {
                names.add(display.getName());
            }
        }
        return new ArrayList<>(names);
    }

    private Integer resolveFirstCollection(Long tourId) {
        HolidaysTourTagRel rel = tourTagRelMapper.selectOne(Wrappers.lambdaQuery(HolidaysTourTagRel.class)
            .eq(HolidaysTourTagRel::getTourId, tourId)
            .orderByAsc(HolidaysTourTagRel::getId)
            .last("limit 1"));
        if (rel == null || rel.getDictCode() == null) {
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
            .ge(HolidaysDeparture::getDepartureDate, LocalDate.now())
            .in(HolidaysDeparture::getStatus, List.of(1, 2))
            .orderByAsc(HolidaysDeparture::getDepartureDate)
            .orderByAsc(HolidaysDeparture::getId)
            .last("limit 1"));
        return departure == null || departure.getDepartureDate() == null ? null : departure.getDepartureDate().format(DATE_FORMATTER);
    }

    private Integer toInteger(Long value) {
        return value == null ? null : value.intValue();
    }

}
