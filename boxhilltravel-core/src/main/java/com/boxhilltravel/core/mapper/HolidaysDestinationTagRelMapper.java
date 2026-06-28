package com.boxhilltravel.core.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysDestinationTagRel;
import com.boxhilltravel.core.domain.bo.HolidaysDestinationTagRelBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationTagRelVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Destination tag relation mapper.
 */
public interface HolidaysDestinationTagRelMapper extends BaseMapperPlus<HolidaysDestinationTagRel, HolidaysDestinationTagRelVo> {

    Page<HolidaysDestinationTagRelVo> selectDestinationTagPage(@Param("page") Page<HolidaysDestinationTagRelVo> page,
                                                              @Param("bo") HolidaysDestinationTagRelBo bo);
}
