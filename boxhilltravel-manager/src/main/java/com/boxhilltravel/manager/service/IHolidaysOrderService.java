package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysOrderBo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

/**
 * Order manager service.
 */
public interface IHolidaysOrderService {

    PageResult<HolidaysOrderVo> queryPageList(HolidaysOrderBo bo, PageQuery pageQuery);

    HolidaysOrderVo queryById(Long id);

    Boolean markCompleted(Long id);

}
