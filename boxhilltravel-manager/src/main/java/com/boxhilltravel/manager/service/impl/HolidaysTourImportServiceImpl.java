package com.boxhilltravel.manager.service.impl;

import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import com.boxhilltravel.core.domain.HolidaysTourItineraryActivity;
import com.boxhilltravel.core.domain.HolidaysTourServiceItem;
import com.boxhilltravel.core.domain.bo.HolidaysReviewBo;
import com.boxhilltravel.core.domain.bo.HolidaysTourBo;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryActivityMapper;
import com.boxhilltravel.core.mapper.HolidaysTourItineraryMapper;
import com.boxhilltravel.core.mapper.HolidaysTourMapper;
import com.boxhilltravel.core.mapper.HolidaysTourServiceItemMapper;
import com.boxhilltravel.manager.domain.dto.TourImportRowResult;
import com.boxhilltravel.manager.domain.excel.DepartureImportRow;
import com.boxhilltravel.manager.domain.excel.ItineraryActivityImportRow;
import com.boxhilltravel.manager.domain.excel.ItineraryImportRow;
import com.boxhilltravel.manager.domain.excel.ReviewImportRow;
import com.boxhilltravel.manager.domain.excel.ServiceItemImportRow;
import com.boxhilltravel.manager.domain.excel.TourImportRow;
import com.boxhilltravel.manager.service.IHolidaysReviewService;
import com.boxhilltravel.manager.service.IHolidaysTourImportService;
import com.boxhilltravel.manager.service.IHolidaysTourService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reads the multi-sheet tour bulk import workbook (Tours / Itinerary / ItineraryActivities /
 * ServiceItems / Departures) and creates each tour with everything linked to it.
 * <p>
 * Note: there's no separate "which destinations does this tour visit" table in this
 * database (holidays_tour_destination doesn't exist) - the app derives that from the
 * Itinerary rows' from/to destinations instead, so this importer does the same.
 * <p>
 * Every tour is created in its own transaction so a mistake on one row doesn't
 * block the rest of the file from importing.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysTourImportServiceImpl implements IHolidaysTourImportService {

    private static final Long DESTINATION_LEVEL_COUNTRY = 2L;

    private final IHolidaysTourService holidaysTourService;
    private final IHolidaysReviewService holidaysReviewService;
    private final HolidaysTourMapper holidaysTourMapper;
    private final HolidaysTourItineraryMapper tourItineraryMapper;
    private final HolidaysTourItineraryActivityMapper tourItineraryActivityMapper;
    private final HolidaysTourServiceItemMapper tourServiceItemMapper;
    private final HolidaysDepartureMapper departureMapper;
    private final HolidaysDestinationMapper destinationMapper;
    private final PlatformTransactionManager transactionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TransactionTemplate transactionTemplate;

    @PostConstruct
    private void initTransactionTemplate() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public List<TourImportRowResult> importTours(MultipartFile file) {
        byte[] bytes = readBytes(file);

        List<TourImportRow> tourRows = readSheet(bytes, "Tours", TourImportRow.class);
        if (tourRows.isEmpty()) {
            throw new ServiceException("The Tours sheet has no data rows");
        }
        Map<String, List<ItineraryImportRow>> itineraryByTour = groupByTourCode(
            readSheet(bytes, "Itinerary", ItineraryImportRow.class), ItineraryImportRow::getTourCode);
        Map<String, List<ItineraryActivityImportRow>> activitiesByTour = groupByTourCode(
            readSheet(bytes, "ItineraryActivities", ItineraryActivityImportRow.class), ItineraryActivityImportRow::getTourCode);
        Map<String, List<ServiceItemImportRow>> serviceItemsByTour = groupByTourCode(
            readSheet(bytes, "ServiceItems", ServiceItemImportRow.class), ServiceItemImportRow::getTourCode);
        Map<String, List<DepartureImportRow>> departuresByTour = groupByTourCode(
            readSheet(bytes, "Departures", DepartureImportRow.class), DepartureImportRow::getTourCode);
        Map<String, List<ReviewImportRow>> reviewsByTour = groupByTourCode(
            readSheet(bytes, "Reviews", ReviewImportRow.class), ReviewImportRow::getTourCode);

        DestinationResolver destinationResolver = new DestinationResolver();
        List<TourImportRowResult> results = new ArrayList<>(tourRows.size());
        for (int i = 0; i < tourRows.size(); i++) {
            // header is row 1, data starts at row 2
            int rowNumber = i + 2;
            TourImportRow row = tourRows.get(i);
            results.add(importOneTour(rowNumber, row, itineraryByTour, activitiesByTour,
                serviceItemsByTour, departuresByTour, reviewsByTour, destinationResolver));
        }
        return results;
    }

    private TourImportRowResult importOneTour(int rowNumber,
                                               TourImportRow row,
                                               Map<String, List<ItineraryImportRow>> itineraryByTour,
                                               Map<String, List<ItineraryActivityImportRow>> activitiesByTour,
                                               Map<String, List<ServiceItemImportRow>> serviceItemsByTour,
                                               Map<String, List<DepartureImportRow>> departuresByTour,
                                               Map<String, List<ReviewImportRow>> reviewsByTour,
                                               DestinationResolver destinationResolver) {
        String tourCode = StringUtils.trim(row.getTourCode());
        try {
            if (StringUtils.isBlank(tourCode)) {
                throw new ServiceException("tour_code is required");
            }
            validateTourRow(row);
            Long existing = holidaysTourMapper.lambda().eq(com.boxhilltravel.core.domain.HolidaysTour::getCode, tourCode).count();
            if (existing != null && existing > 0) {
                throw new ServiceException("tour_code '" + tourCode + "' already exists, skipped");
            }

            Long tourId = transactionTemplate.execute(status -> {
                Long id = insertTour(row, tourCode);
                Map<Integer, Long> itineraryIdByDay = insertItinerary(id,
                    itineraryByTour.getOrDefault(tourCode, List.of()), destinationResolver);
                insertItineraryActivities(id, itineraryIdByDay,
                    activitiesByTour.getOrDefault(tourCode, List.of()));
                insertServiceItems(id, serviceItemsByTour.getOrDefault(tourCode, List.of()));
                insertDepartures(id, departuresByTour.getOrDefault(tourCode, List.of()));
                insertReviews(id, reviewsByTour.getOrDefault(tourCode, List.of()));
                return id;
            });
            log.info("Imported tour {} (row {}) as id {}", tourCode, rowNumber, tourId);
            return TourImportRowResult.ok(rowNumber, tourCode);
        } catch (Exception e) {
            log.warn("Failed to import tour row {} ({}): {}", rowNumber, tourCode, e.getMessage());
            return TourImportRowResult.fail(rowNumber, tourCode, e.getMessage());
        }
    }

    private void validateTourRow(TourImportRow row) {
        requireNotBlank(row.getName(), "name");
        requireNotNull(row.getDurationDays(), "duration_days");
        requireNotNull(row.getTravelStyle(), "travel_style (check the value matches the dashboard's dropdown exactly)");
        requireNotNull(row.getServiceLevel(), "service_level (check the value matches the dashboard's dropdown exactly)");
        requireNotNull(row.getPhysicalRating(), "physical_rating (check the value matches the dashboard's dropdown exactly)");
        requireNotNull(row.getTripType(), "trip_type (check the value matches the dashboard's dropdown exactly)");
        requireNotNull(row.getBasePrice(), "base_price");
        requireNotNull(row.getSalePrice(), "sale_price");
        requireNotNull(row.getStatus(), "status (use Draft, Published or Off-shelf)");
    }

    private Long insertTour(TourImportRow row, String tourCode) {
        HolidaysTourBo bo = new HolidaysTourBo();
        bo.setCode(tourCode);
        bo.setName(row.getName());
        bo.setDescription(row.getDescription());
        bo.setDurationDays(row.getDurationDays());
        bo.setTravelStyle(row.getTravelStyle());
        bo.setServiceLevel(row.getServiceLevel());
        bo.setPhysicalRating(row.getPhysicalRating());
        bo.setTripType(row.getTripType());
        bo.setMinAge(row.getMinAge());
        bo.setBasePrice(row.getBasePrice());
        bo.setSalePrice(row.getSalePrice());
        bo.setSingleSupplement(row.getSingleSupplement());
        bo.setCurrency(row.getCurrency());
        bo.setNotes(row.getNotes());
        bo.setLatestArrivalTime(row.getLatestArrivalTime());
        bo.setEarliestDepartureTime(row.getEarliestDepartureTime());
        bo.setStatus(row.getStatus());
        bo.setSeoTitle(row.getSeoTitle());
        bo.setSeoDescription(row.getSeoDescription());
        bo.setSeoKeywords(row.getSeoKeywords());
        bo.setCollectionTag(row.getCollectionTag());
        holidaysTourService.insertByBo(bo);
        return bo.getId();
    }

    private Map<Integer, Long> insertItinerary(Long tourId, List<ItineraryImportRow> rows, DestinationResolver resolver) {
        Map<Integer, Long> itineraryIdByDay = new HashMap<>();
        for (ItineraryImportRow row : rows) {
            requireNotNull(row.getDayNumber(), "itinerary day_number for tour");
            requireNotBlank(row.getTitle(), "itinerary title for day " + row.getDayNumber());
            HolidaysTourItinerary entity = new HolidaysTourItinerary();
            entity.setTourId(tourId);
            entity.setDayNumber(row.getDayNumber());
            entity.setTitle(row.getTitle());
            entity.setDescription(row.getDescription());
            if (StringUtils.isNotBlank(row.getFromCountry())) {
                Long fromId = resolver.resolve(row.getFromCountry());
                entity.setFromDestinationId(fromId == null ? null : fromId.intValue());
            }
            if (StringUtils.isNotBlank(row.getToCountry())) {
                Long toId = resolver.resolve(row.getToCountry());
                entity.setToDestinationId(toId == null ? null : toId.intValue());
            }
            entity.setMeals(buildMeals(row.getBreakfast(), row.getLunch(), row.getDinner()));
            tourItineraryMapper.insert(entity);
            itineraryIdByDay.put(row.getDayNumber(), entity.getId());
        }
        return itineraryIdByDay;
    }

    private void insertItineraryActivities(Long tourId, Map<Integer, Long> itineraryIdByDay,
                                            List<ItineraryActivityImportRow> rows) {
        int index = 0;
        for (ItineraryActivityImportRow row : rows) {
            index++;
            requireNotNull(row.getDayNumber(), "itinerary activity day_number for tour");
            requireNotBlank(row.getTitle(), "itinerary activity title for day " + row.getDayNumber());
            Long itineraryId = itineraryIdByDay.get(row.getDayNumber());
            if (itineraryId == null) {
                throw new ServiceException("itinerary activity day_number " + row.getDayNumber()
                    + " doesn't match any day on the Itinerary sheet for this tour");
            }
            HolidaysTourItineraryActivity entity = new HolidaysTourItineraryActivity();
            entity.setTourId(tourId);
            entity.setItineraryId(itineraryId);
            entity.setTitle(row.getTitle());
            entity.setDescription(StringUtils.blankToDefault(row.getDescription(), ""));
            entity.setActivityIcon(StringUtils.trim(row.getActivityIcon()));
            entity.setSubtitle(toSubtitleJson(row.getSubtitle()));
            entity.setSortOrder(row.getSortOrder() != null ? row.getSortOrder() : index);
            entity.setShowInPreview(isYes(row.getShowInPreview()) ? 1L : 0L);
            tourItineraryActivityMapper.insert(entity);
        }
    }

    private String toSubtitleJson(String subtitle) {
        if (StringUtils.isBlank(subtitle)) {
            return null;
        }
        List<String> tags = java.util.Arrays.stream(subtitle.split("[,;]"))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .toList();
        if (tags.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            throw new ServiceException("Could not encode subtitle '" + subtitle + "': " + e.getMessage());
        }
    }

    // dict "dining": 1=Breakfast, 2=Dinner, 3=Lunch - matches real data (e.g. ["1","3"]),
    // stored as a JSON array of these codes, not a plain "B,L,D" string
    private String buildMeals(String breakfast, String lunch, String dinner) {
        List<String> codes = new ArrayList<>(3);
        if (isYes(breakfast)) {
            codes.add("1");
        }
        if (isYes(dinner)) {
            codes.add("2");
        }
        if (isYes(lunch)) {
            codes.add("3");
        }
        if (codes.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(codes);
        } catch (Exception e) {
            throw new ServiceException("Could not encode meals: " + e.getMessage());
        }
    }

    private boolean isYes(String value) {
        return StringUtils.equalsAnyIgnoreCase(StringUtils.trim(value), "Y", "YES", "TRUE", "1");
    }

    private void insertServiceItems(Long tourId, List<ServiceItemImportRow> rows) {
        int index = 0;
        for (ServiceItemImportRow row : rows) {
            index++;
            requireNotNull(row.getType(), "service item 'type' (use Included or Excluded) for tour");
            requireNotBlank(row.getContent(), "service item content for tour");
            HolidaysTourServiceItem entity = new HolidaysTourServiceItem();
            entity.setTourId(tourId);
            entity.setItemType(row.getType());
            entity.setContent(row.getContent());
            entity.setSortOrder(row.getSortOrder() != null ? row.getSortOrder() : index);
            tourServiceItemMapper.insert(entity);
        }
    }

    private void insertDepartures(Long tourId, List<DepartureImportRow> rows) {
        for (DepartureImportRow row : rows) {
            requireNotNull(row.getMaxCapacity(), "departure max_capacity");
            requireNotNull(row.getBasePrice(), "departure base_price");
            requireNotNull(row.getSalePrice(), "departure sale_price");
            LocalDate departureDate = parseDate(row.getDepartureDate(), "departure_date");
            LocalDate returnDate = parseDate(row.getReturnDate(), "return_date");
            HolidaysDeparture entity = new HolidaysDeparture();
            entity.setTourId(tourId);
            entity.setDepartureDate(departureDate);
            entity.setReturnDate(returnDate);
            if (departureDate != null && returnDate != null) {
                entity.setDurationDays(ChronoUnit.DAYS.between(departureDate, returnDate));
            }
            entity.setMaxCapacity(row.getMaxCapacity());
            entity.setMinCapacity(row.getMinCapacity());
            // no default in the DB for these two - the dedicated departure service sets
            // them the same way, so we mirror that here
            entity.setBookedCount(0L);
            entity.setAvailableCount(row.getMaxCapacity());
            entity.setBasePrice(row.getBasePrice());
            entity.setSalePrice(row.getSalePrice());
            // discount_rate is never entered manually in the dashboard either - it's always
            // sale_price / base_price, computed there and mirrored here
            entity.setDiscountRate(computeDiscountRate(row.getBasePrice(), row.getSalePrice()));
            entity.setStatus(row.getStatus() != null ? row.getStatus() : 1);
            entity.setDepartureType(row.getDepartureType() != null ? row.getDepartureType() : 1);
            departureMapper.insert(entity);
        }
    }

    private BigDecimal computeDiscountRate(BigDecimal basePrice, BigDecimal salePrice) {
        if (basePrice == null || salePrice == null || basePrice.signum() == 0) {
            return null;
        }
        return salePrice.divide(basePrice, 2, RoundingMode.HALF_UP);
    }

    private void insertReviews(Long tourId, List<ReviewImportRow> rows) {
        int index = 0;
        for (ReviewImportRow row : rows) {
            index++;
            requireNotBlank(row.getNickname(), "review nickname for tour");
            requireNotNull(row.getRating(), "review rating (1-5) for tour");
            requireNotBlank(row.getTitle(), "review title for tour");
            requireNotBlank(row.getContent(), "review content for tour");
            HolidaysReviewBo bo = new HolidaysReviewBo();
            bo.setTourId(tourId);
            bo.setNickname(row.getNickname());
            bo.setRating(row.getRating());
            bo.setTitle(row.getTitle());
            bo.setContent(row.getContent());
            bo.setSortOrder(index);
            bo.setFeatured(isYes(row.getFeatured()) ? 1 : 0);
            // source/status default to manager-entry/published inside insertByBo, matching
            // exactly what the dashboard's own "add review" form does
            holidaysReviewService.insertByBo(bo);
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            throw new ServiceException(fieldName + " '" + value + "' is not a valid date (use YYYY-MM-DD)");
        }
    }

    private void requireNotBlank(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(fieldName + " is required");
        }
    }

    private void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ServiceException(fieldName + " is required");
        }
    }

    private <T> List<T> readSheet(byte[] bytes, String sheetName, Class<T> clazz) {
        return ExcelBuilder.read(new ByteArrayInputStream(bytes), clazz)
            .sheetName(sheetName)
            .doReadSync();
    }

    private <T> Map<String, List<T>> groupByTourCode(List<T> rows, java.util.function.Function<T, String> tourCodeExtractor) {
        return rows.stream()
            .filter(row -> StringUtils.isNotBlank(tourCodeExtractor.apply(row)))
            .collect(Collectors.groupingBy(
                row -> StringUtils.trim(tourCodeExtractor.apply(row)),
                java.util.LinkedHashMap::new,
                Collectors.toList()));
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new ServiceException("Could not read the uploaded file: " + e.getMessage());
        }
    }

    /**
     * Resolves a country name from the spreadsheet into a holidays_destination id (at the
     * country level - every existing tour links its itinerary to countries only, never
     * specific cities, so the importer matches that convention), caching lookups since the
     * same country is usually referenced many times in one file.
     */
    private class DestinationResolver {

        private final Map<String, Long> cache = new HashMap<>();

        Long resolve(String country) {
            String key = StringUtils.trim(country);
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
            Long resolved = doResolve(key);
            cache.put(key, resolved);
            return resolved;
        }

        private Long doResolve(String countryName) {
            if (StringUtils.isBlank(countryName)) {
                return null;
            }
            List<HolidaysDestination> candidates = destinationMapper.lambda()
                .eq(HolidaysDestination::getName, countryName)
                .list();
            if (candidates.isEmpty()) {
                throw new ServiceException("Destination not found: '" + countryName + "'");
            }
            return candidates.stream()
                .filter(d -> DESTINATION_LEVEL_COUNTRY.equals(d.getLevel()))
                .findFirst()
                .orElse(candidates.get(0))
                .getId();
        }

    }

}
