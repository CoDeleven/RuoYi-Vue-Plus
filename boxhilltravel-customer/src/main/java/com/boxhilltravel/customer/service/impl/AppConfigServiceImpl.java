package com.boxhilltravel.customer.service.impl;

import com.boxhilltravel.customer.domain.vo.AppConfigVo;
import com.boxhilltravel.customer.domain.vo.EnumItemVo;
import com.boxhilltravel.customer.domain.vo.SortedItemVo;
import com.boxhilltravel.customer.service.IAppConfigService;
import lombok.RequiredArgsConstructor;
import org.dromara.system.domain.vo.SysDictDataVo;
import org.dromara.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer application config service implementation.
 */
@RequiredArgsConstructor
@Service
public class AppConfigServiceImpl implements IAppConfigService {

    private final ISysDictTypeService dictTypeService;

    @Override
    public AppConfigVo getAppConfig() {
        AppConfigVo vo = new AppConfigVo();
        Map<String, List<EnumItemVo>> enums = new LinkedHashMap<>();
        enums.put("travelStyle", buildItemsFromDict("holidays_tour_travel_style"));
        enums.put("travelCollection", buildItemsFromDict("holidays_tour_collection"));
        enums.put("travelDuration", buildTravelDurationItems());
        enums.put("travelTripType", buildItemsFromDict("holidays_tour_trip_type"));
        enums.put("travelPhysicalRating", buildItemsFromDict("holidays_tour_physical_rating"));
        enums.put("travelServiceLevel", buildItemsFromDict("holidays_tour_service_level"));
        enums.put("dining", buildItemsFromDict("dining"));
        enums.put("currency", buildItemsFromDict("holidays_currency_unit"));
        enums.put("destinationLanguage", buildItemsFromDict("holidays_destination_language"));
        enums.put("destinationTimeZone", buildItemsFromDict("holidays_destination_time_zone"));
        vo.setEnums(enums);

        Map<String, List<SortedItemVo>> sortTypes = new LinkedHashMap<>();
        sortTypes.put("travelSortMethods", buildTravelSortMethodsItems());
        vo.setSortTypes(sortTypes);
        return vo;
    }

    private List<EnumItemVo> buildItemsFromDict(String dictType) {
        List<EnumItemVo> items = new ArrayList<>();
        for (SysDictDataVo data : dictTypeService.selectDictDataByType(dictType)) {
            items.add(new EnumItemVo(data.getDictValue(), data.getDictLabel(), data.getRemark()));
        }
        return items;
    }

    private List<EnumItemVo> buildTravelDurationItems() {
        return List.of(
            new EnumItemVo("1", "1-3 days", ""),
            new EnumItemVo("2", "4-6 days", ""),
            new EnumItemVo("3", "7-9 days", ""),
            new EnumItemVo("4", "8-13 days", ""),
            new EnumItemVo("5", "14-21 days", ""),
            new EnumItemVo("6", "22-28 days", ""),
            new EnumItemVo("7", "29+ days", "")
        );
    }

    private List<SortedItemVo> buildTravelSortMethodsItems() {
        return List.of(
            new SortedItemVo("created_at,desc", "Relevance", ""),
            new SortedItemVo("sale_price,desc", "Price: High to Low", ""),
            new SortedItemVo("sale_price,asc", "Price: Low to High", ""),
            new SortedItemVo("duration_days,desc", "Duration Longest First", ""),
            new SortedItemVo("duration_days,asc", "Duration Shortest First", "")
        );
    }

}
