package com.boxhilltravel.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import com.boxhilltravel.core.domain.HolidaysTourItineraryActivity;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryActivityMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryMapper;
import com.boxhilltravel.core.mapper.HolidaysTourMapper;
import com.boxhilltravel.customer.domain.bo.CheckoutQuoteBo;
import com.boxhilltravel.customer.domain.vo.CheckoutBootstrapVo;
import com.boxhilltravel.customer.domain.vo.CheckoutQuoteVo;
import com.boxhilltravel.customer.domain.vo.DepartureItemVo;
import com.boxhilltravel.customer.domain.vo.QuoteLineItemVo;
import com.boxhilltravel.customer.domain.vo.TourDetailVo;
import com.boxhilltravel.customer.domain.vo.TourExtraDayOptionVo;
import com.boxhilltravel.customer.domain.vo.TourExtraOptionVo;
import com.boxhilltravel.customer.service.ICheckoutService;
import com.boxhilltravel.customer.service.IHolidaysTourCustomerService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Customer checkout service implementation.
 */
@RequiredArgsConstructor
@Service
public class CheckoutServiceImpl implements ICheckoutService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final IHolidaysTourCustomerService tourCustomerService;
    private final HolidaysTourMapper tourMapper;
    private final HolidaysDepartureMapper departureMapper;
    private final HolidaysTourItineraryMapper itineraryMapper;
    private final HolidaysTourItineraryActivityMapper activityMapper;

    @Override
    public CheckoutBootstrapVo bootstrap(Long tourId, Long departureId, Integer travelerCount) {
        TourDetailVo tour = tourCustomerService.queryDetail(tourId);
        if (tour == null) {
            throw new ServiceException("Tour not found");
        }
        List<DepartureItemVo> departures = queryDepartures(tourId);
        Long selectedDepartureId = departureId != null ? departureId : departures.stream().findFirst().map(DepartureItemVo::getId).orElse(null);
        DepartureItemVo selectedDeparture = departures.stream()
            .filter(item -> item.getId().equals(selectedDepartureId))
            .findFirst()
            .orElse(null);

        CheckoutBootstrapVo vo = new CheckoutBootstrapVo();
        vo.setTour(tour);
        vo.setDepartures(departures);
        vo.setSelectedDeparture(selectedDeparture);
        vo.setTourExtras(queryTourExtraOptions(tourId));
        if (selectedDepartureId != null) {
            CheckoutQuoteBo quoteBo = new CheckoutQuoteBo();
            quoteBo.setTourId(tourId);
            quoteBo.setDepartureId(selectedDepartureId);
            quoteBo.setTravelerCount(travelerCount == null ? 1 : travelerCount);
            vo.setQuote(quote(quoteBo));
        }
        return vo;
    }

    @Override
    public CheckoutQuoteVo quote(CheckoutQuoteBo bo) {
        QuoteContext context = buildQuoteContext(bo);
        CheckoutQuoteVo vo = new CheckoutQuoteVo();
        vo.setCurrency(context.currency());
        vo.setUnitPrice(context.unitPrice());
        vo.setTourAmount(context.tourAmount());
        vo.setExtrasAmount(context.extrasAmount());
        vo.setTotalAmount(context.tourAmount().add(context.extrasAmount()));
        vo.setTaxesIncluded(true);
        vo.setLineItems(context.lineItems());
        return vo;
    }

    QuoteContext buildQuoteContext(CheckoutQuoteBo bo) {
        if (bo == null) {
            throw new ServiceException("Quote request is required");
        }
        int travelerCount = validateTravelerCount(bo.getTravelerCount());
        HolidaysTour tour = tourMapper.selectById(bo.getTourId());
        if (tour == null || !Long.valueOf(1L).equals(tour.getStatus()) || tour.getDeletedAt() != null) {
            throw new ServiceException("Tour is not available");
        }
        HolidaysDeparture departure = departureMapper.selectById(bo.getDepartureId());
        validateDeparture(tour.getId(), departure, travelerCount);

        BigDecimal unitPrice = departure.getSalePrice() != null ? departure.getSalePrice() : departure.getBasePrice();
        if (unitPrice == null) {
            unitPrice = tour.getSalePrice() != null ? tour.getSalePrice() : tour.getBasePrice();
        }
        if (unitPrice == null) {
            unitPrice = ZERO;
        }
        BigDecimal tourAmount = unitPrice.multiply(BigDecimal.valueOf(travelerCount));
        List<QuoteLineItemVo> lineItems = new ArrayList<>();
        QuoteLineItemVo tourLine = new QuoteLineItemVo();
        tourLine.setType("TOUR");
        tourLine.setTitle(tour.getName());
        tourLine.setQuantity(travelerCount);
        tourLine.setAmount(tourAmount);
        lineItems.add(tourLine);

        BigDecimal extrasAmount = ZERO;
        return new QuoteContext(tour, departure, travelerCount, tour.getCurrency(),
            unitPrice, tourAmount, extrasAmount, lineItems);
    }

    private List<TourExtraDayOptionVo> queryTourExtraOptions(Long tourId) {
        List<HolidaysTourItinerary> itineraries = itineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
            .eq(HolidaysTourItinerary::getTourId, tourId)
            .orderByAsc(HolidaysTourItinerary::getDayNumber)
            .orderByAsc(HolidaysTourItinerary::getId));
        if (itineraries.isEmpty()) {
            return List.of();
        }
        Map<Long, List<TourExtraOptionVo>> activityMap = activityMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItineraryActivity.class)
                .eq(HolidaysTourItineraryActivity::getTourId, tourId)
                .orderByAsc(HolidaysTourItineraryActivity::getItineraryId)
                .orderByAsc(HolidaysTourItineraryActivity::getSortOrder)
                .orderByAsc(HolidaysTourItineraryActivity::getId))
            .stream()
            .collect(Collectors.groupingBy(HolidaysTourItineraryActivity::getItineraryId,
                Collectors.mapping(this::toTourExtraActivityVo, Collectors.toList())));
        return itineraries.stream().map(itinerary -> {
            TourExtraDayOptionVo vo = new TourExtraDayOptionVo();
            vo.setId(itinerary.getId());
            vo.setDayNo(itinerary.getDayNumber());
            vo.setTitle(itinerary.getTitle());
            vo.setDescription(itinerary.getDescription());
            vo.setActivities(activityMap.getOrDefault(itinerary.getId(), List.of()));
            return vo;
        }).toList();
    }

    private TourExtraOptionVo toTourExtraActivityVo(HolidaysTourItineraryActivity activity) {
        TourExtraOptionVo vo = new TourExtraOptionVo();
        vo.setId(activity.getId());
        vo.setItineraryId(activity.getItineraryId());
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        if (StringUtils.isNotBlank(activity.getSubtitle())) {
            vo.setSubtitle(JsonUtils.parseArray(activity.getSubtitle(), String.class));
        }
        if (StringUtils.isNotBlank(activity.getActivityIcon())) {
            vo.setIconPath(activity.getActivityIcon());
        }
        return vo;
    }

    private List<DepartureItemVo> queryDepartures(Long tourId) {
        return departureMapper.selectList(Wrappers.lambdaQuery(HolidaysDeparture.class)
                .eq(HolidaysDeparture::getTourId, tourId)
                .orderByAsc(HolidaysDeparture::getDepartureDate)
                .orderByAsc(HolidaysDeparture::getId))
            .stream().map(this::toDepartureItemVo).toList();
    }

    private DepartureItemVo toDepartureItemVo(HolidaysDeparture departure) {
        DepartureItemVo vo = new DepartureItemVo();
        vo.setId(departure.getId());
        vo.setStartDate(departure.getDepartureDate() == null ? null : departure.getDepartureDate().format(DATE_FORMATTER));
        vo.setEndDate(departure.getReturnDate() == null ? null : departure.getReturnDate().format(DATE_FORMATTER));
        vo.setDuration(departure.getDurationDays() == null ? null : departure.getDurationDays().intValue());
        vo.setBasePrice(departure.getBasePrice());
        vo.setSalePrice(departure.getSalePrice());
        vo.setAvailableCount(departure.getAvailableCount() == null ? null : departure.getAvailableCount().intValue());
        vo.setStatus(departure.getStatus());
        return vo;
    }

    private int validateTravelerCount(Integer travelerCount) {
        if (travelerCount == null || travelerCount < 1 || travelerCount > 10) {
            throw new ServiceException("Traveler count must be between 1 and 10");
        }
        return travelerCount;
    }

    private void validateDeparture(Long tourId, HolidaysDeparture departure, int travelerCount) {
        if (departure == null || !tourId.equals(departure.getTourId())) {
            throw new ServiceException("Departure not found");
        }
        if (!Integer.valueOf(1).equals(departure.getStatus())) {
            throw new ServiceException("Departure is not available");
        }
        if (departure.getAvailableCount() == null) {
            throw new ServiceException("Departure inventory is not configured");
        }
        if (departure.getAvailableCount() < travelerCount) {
            throw new ServiceException("Not enough seats available");
        }
    }

    record QuoteContext(HolidaysTour tour, HolidaysDeparture departure, int travelerCount, String currency,
                        BigDecimal unitPrice, BigDecimal tourAmount, BigDecimal extrasAmount,
                        List<QuoteLineItemVo> lineItems) {
    }

}
