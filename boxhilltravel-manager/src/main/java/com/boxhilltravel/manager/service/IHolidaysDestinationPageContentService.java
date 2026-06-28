package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysDestinationPageContentBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationPageContentVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * Destination country page content service interface.
 */
public interface IHolidaysDestinationPageContentService {

    HolidaysDestinationPageContentVo queryById(Long id);

    PageResult<HolidaysDestinationPageContentVo> queryPageList(HolidaysDestinationPageContentBo bo, PageQuery pageQuery);

    List<HolidaysDestinationPageContentVo> queryList(HolidaysDestinationPageContentBo bo);

    Boolean insertByBo(HolidaysDestinationPageContentBo bo);

    Boolean updateByBo(HolidaysDestinationPageContentBo bo);

    Boolean updateStatus(Long id, Long status);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
