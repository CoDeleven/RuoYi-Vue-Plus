package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysDestinationTagRelBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationTagRelVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * Destination Tag Service interface.
 */
public interface IHolidaysDestinationTagRelService {

    HolidaysDestinationTagRelVo queryById(Long id);

    PageResult<HolidaysDestinationTagRelVo> queryPageList(HolidaysDestinationTagRelBo bo, PageQuery pageQuery);

    List<HolidaysDestinationTagRelVo> queryList(HolidaysDestinationTagRelBo bo);

    Boolean insertByBo(HolidaysDestinationTagRelBo bo);

    Boolean updateByBo(HolidaysDestinationTagRelBo bo);

    Boolean updateSort(Long id, Integer sortValue);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
