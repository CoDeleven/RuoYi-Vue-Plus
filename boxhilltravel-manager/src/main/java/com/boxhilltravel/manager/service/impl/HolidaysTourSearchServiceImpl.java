package com.boxhilltravel.manager.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import com.boxhilltravel.core.domain.HolidaysTourSearch;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryMapper;
import com.boxhilltravel.core.mapper.HolidaysTourMapper;
import com.boxhilltravel.core.mapper.HolidaysTourSearchMapper;
import com.boxhilltravel.manager.service.IHolidaysTourSearchService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * H5 tour search read model service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysTourSearchServiceImpl implements IHolidaysTourSearchService {

    private static final long DESTINATION_LEVEL_COUNTRY = 2L;
    private static final long DESTINATION_LEVEL_CITY = 3L;

    private final HolidaysTourMapper tourMapper;
    private final HolidaysTourItineraryMapper itineraryMapper;
    private final HolidaysDestinationMapper destinationMapper;
    private final HolidaysTourSearchMapper tourSearchMapper;

    @Override
    public void rebuildTourSearch(Long tourId) {
        if (tourId == null) {
            return;
        }
        HolidaysTour tour = tourMapper.selectById(tourId);
        if (tour == null || tour.getDeletedAt() != null) {
            deleteTourSearch(tourId);
            return;
        }
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        addTerm(terms, tour.getCode());
        addTerm(terms, tour.getName());
        appendItineraryDestinations(tourId, terms);

        HolidaysTourSearch search = new HolidaysTourSearch();
        search.setTourId(tourId);
        search.setCode(tour.getCode());
        search.setName(tour.getName());
        search.setSearchText(String.join(" ", terms));
        search.setStatus(tour.getStatus());
        search.setUpdatedAt(LocalDateTime.now());

        if (tourSearchMapper.selectById(tourId) == null) {
            tourSearchMapper.insert(search);
        } else {
            tourSearchMapper.updateById(search);
        }
    }

    @Override
    public void deleteTourSearch(Long tourId) {
        if (tourId != null) {
            tourSearchMapper.deleteById(tourId);
        }
    }

    private void appendItineraryDestinations(Long tourId, LinkedHashSet<String> terms) {
        List<HolidaysTourItinerary> itineraries = itineraryMapper.selectList(Wrappers.lambdaQuery(HolidaysTourItinerary.class)
            .eq(HolidaysTourItinerary::getTourId, tourId)
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
                .in(HolidaysDestination::getId, destinationIds)
                .isNull(HolidaysDestination::getDeletedAt))
            .stream()
            .collect(Collectors.toMap(HolidaysDestination::getId, item -> item, (first, second) -> first));
        Set<Long> parentCountryIds = destinationMap.values().stream()
            .filter(item -> Objects.equals(item.getLevel(), DESTINATION_LEVEL_CITY))
            .map(HolidaysDestination::getParentId)
            .filter(Objects::nonNull)
            .filter(id -> !destinationMap.containsKey(id))
            .collect(Collectors.toSet());
        if (!parentCountryIds.isEmpty()) {
            destinationMapper.selectList(Wrappers.lambdaQuery(HolidaysDestination.class)
                    .in(HolidaysDestination::getId, parentCountryIds)
                    .isNull(HolidaysDestination::getDeletedAt))
                .forEach(item -> destinationMap.put(item.getId(), item));
        }
        itineraries.forEach(item -> {
            appendDestinationTerms(item.getFromDestinationId(), destinationMap, terms);
            appendDestinationTerms(item.getToDestinationId(), destinationMap, terms);
        });
    }

    private void appendDestinationTerms(Integer destinationId, Map<Long, HolidaysDestination> destinationMap, LinkedHashSet<String> terms) {
        if (destinationId == null) {
            return;
        }
        HolidaysDestination destination = destinationMap.get(destinationId.longValue());
        if (destination == null) {
            return;
        }
        addDestinationNameTerms(destination, terms);
        if (Objects.equals(destination.getLevel(), DESTINATION_LEVEL_CITY)) {
            HolidaysDestination country = destinationMap.get(destination.getParentId());
            addDestinationNameTerms(country, terms);
        } else if (Objects.equals(destination.getLevel(), DESTINATION_LEVEL_COUNTRY)) {
            addDestinationNameTerms(destination, terms);
        }
    }

    private void addDestinationNameTerms(HolidaysDestination destination, LinkedHashSet<String> terms) {
        if (destination == null) {
            return;
        }
        addTerm(terms, destination.getName());
        addTerm(terms, destination.getNameEn());
    }

    private void addTerm(LinkedHashSet<String> terms, String term) {
        if (StringUtils.isBlank(term)) {
            return;
        }
        terms.add(StringUtils.trim(term));
    }

}
