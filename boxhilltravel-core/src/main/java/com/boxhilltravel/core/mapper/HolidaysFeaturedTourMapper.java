package com.boxhilltravel.core.mapper;

import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.HolidaysFeaturedTour;
import com.boxhilltravel.core.domain.vo.HolidaysFeaturedTourVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 精选线路Mapper接口
 *
 * @author Lion Li
 * @date 2026-06-29 00:06:41
 */
public interface HolidaysFeaturedTourMapper extends BaseMapperPlus<HolidaysFeaturedTour, HolidaysFeaturedTourVo> {

    /**
     * Query public customer featured tours.
     *
     * @param limit max rows
     * @return tour list
     */
    List<HolidaysTour> selectFeaturedToursForCustomer(@Param("limit") Integer limit);

}



