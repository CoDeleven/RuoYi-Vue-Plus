package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.bo.QueryTourBo;
import com.boxhilltravel.customer.domain.vo.FeaturedTourVo;
import com.boxhilltravel.customer.domain.vo.HotDealTourVo;
import com.boxhilltravel.customer.domain.vo.TourDetailVo;
import com.boxhilltravel.customer.domain.vo.TourListItemVo;
import org.dromara.common.core.domain.PageResult;

import java.util.List;

/**
 * Customer tour service.
 */
public interface IHolidaysTourCustomerService {

    PageResult<TourListItemVo> queryPageList(QueryTourBo bo);

    TourDetailVo queryDetail(Long id);

    List<HotDealTourVo> queryHotTours(Integer limit);

    List<FeaturedTourVo> queryFeaturedTours(Integer limit);

}
