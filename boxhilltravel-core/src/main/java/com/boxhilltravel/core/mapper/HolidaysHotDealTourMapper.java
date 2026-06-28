package com.boxhilltravel.core.mapper;

import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.HolidaysHotDealTour;
import com.boxhilltravel.core.domain.vo.HolidaysHotDealTourVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 热卖线路Mapper接口
 *
 * @author Lion Li
 * @date 2026-06-28 23:38:47
 */
public interface HolidaysHotDealTourMapper extends BaseMapperPlus<HolidaysHotDealTour, HolidaysHotDealTourVo> {

    /**
     * Query public customer hot deal tours.
     *
     * @param limit max rows
     * @return tour list
     */
    List<HolidaysTour> selectHotToursForCustomer(@Param("limit") Integer limit);

}



