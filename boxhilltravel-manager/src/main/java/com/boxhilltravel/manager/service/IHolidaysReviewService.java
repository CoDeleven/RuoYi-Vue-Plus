package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysReviewBo;
import com.boxhilltravel.core.domain.vo.HolidaysReviewVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;

/**
 * Tour review manager service.
 */
public interface IHolidaysReviewService {

    HolidaysReviewVo queryById(Long id);

    PageResult<HolidaysReviewVo> queryPageList(HolidaysReviewBo bo, PageQuery pageQuery);

    Boolean insertByBo(HolidaysReviewBo bo);

    Boolean updateByBo(HolidaysReviewBo bo);

    Boolean audit(Long id, Integer status, String rejectReason);

    Boolean updateFeatured(Long id, Integer featured);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
