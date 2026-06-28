package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.bo.CreateReviewBo;
import com.boxhilltravel.customer.domain.vo.ReviewItemVo;
import com.boxhilltravel.customer.domain.vo.ReviewSummaryVo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.List;

/**
 * Customer review service.
 */
public interface IHolidaysReviewCustomerService {

    ReviewSummaryVo queryTourReviews(Long tourId, PageQuery pageQuery);

    List<ReviewItemVo> queryHomeReviews(Integer limit);

    ReviewItemVo create(CreateReviewBo bo);

}
