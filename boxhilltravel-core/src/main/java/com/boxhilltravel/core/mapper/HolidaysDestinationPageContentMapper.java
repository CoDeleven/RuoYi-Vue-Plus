package com.boxhilltravel.core.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysDestinationPageContent;
import com.boxhilltravel.core.domain.bo.HolidaysDestinationPageContentBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationPageContentVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Destination country page content mapper.
 */
public interface HolidaysDestinationPageContentMapper extends BaseMapperPlus<HolidaysDestinationPageContent, HolidaysDestinationPageContentVo> {

    Page<HolidaysDestinationPageContentVo> selectDestinationPageContentPage(
        @Param("page") Page<HolidaysDestinationPageContentVo> page,
        @Param("bo") HolidaysDestinationPageContentBo bo);

}
