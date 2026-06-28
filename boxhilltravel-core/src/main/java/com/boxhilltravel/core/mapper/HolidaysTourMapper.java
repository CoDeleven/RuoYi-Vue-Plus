package com.boxhilltravel.core.mapper;


import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.query.CustomerTourQuery;
import com.boxhilltravel.core.domain.vo.HolidaysTourVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;


/**
 * 线路管理Mapper接口
 *
 * @author CoDeleven
 * @date 2026-06-26 18:26:30
 */
public interface HolidaysTourMapper extends BaseMapperPlus<HolidaysTour, HolidaysTourVo> {

    /**
     * Query public customer tour page.
     *
     * @param page page request
     * @param query query condition
     * @return tour page
     */
    Page<HolidaysTour> selectCustomerTourPage(@Param("page") Page<HolidaysTour> page, @Param("query") CustomerTourQuery query);

    /**
     * Query lowest price public customer tour by destination ids.
     *
     * @param destinationIds destination ids
     * @return lowest price tour
     */
    HolidaysTour selectLowestPriceTourByDestinationIds(@Param("destinationIds") List<Long> destinationIds);

}



