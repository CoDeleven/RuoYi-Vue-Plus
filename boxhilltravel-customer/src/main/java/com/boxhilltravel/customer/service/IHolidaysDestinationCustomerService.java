package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.vo.DestinationVo;
import com.boxhilltravel.customer.domain.vo.PopularDestinationVo;

import java.util.List;
import java.util.Map;

/**
 * Customer destination service.
 */
public interface IHolidaysDestinationCustomerService {

    List<DestinationVo> queryDestinationTree();

    List<DestinationVo> queryChildrenByParentId(Long parentId);

    List<DestinationVo> queryAvailableTree(String destinationNameEn);

    DestinationVo queryDestination(String destinationNameEn);

    List<PopularDestinationVo> queryPopularDestinations(Integer limit);

    List<DestinationVo> queryFeaturedDestinations();

    Map<Long, Long> queryTourCounts(List<Long> countryIds);

}
