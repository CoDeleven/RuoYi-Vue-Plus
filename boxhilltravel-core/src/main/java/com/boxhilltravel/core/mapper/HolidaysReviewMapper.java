package com.boxhilltravel.core.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysReview;
import com.boxhilltravel.core.domain.bo.HolidaysReviewBo;
import com.boxhilltravel.core.domain.vo.HolidaysReviewVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * Tour review mapper.
 */
public interface HolidaysReviewMapper extends BaseMapperPlus<HolidaysReview, HolidaysReviewVo> {

    Page<HolidaysReviewVo> selectReviewVoPage(@Param("page") Page<HolidaysReviewVo> page, @Param("bo") HolidaysReviewBo bo);

    HolidaysReviewVo selectReviewVoById(@Param("id") Long id);

    Page<HolidaysReviewVo> selectPublishedReviewVoPage(@Param("page") Page<HolidaysReviewVo> page, @Param("tourId") Long tourId);

    List<HolidaysReviewVo> selectHomeReviewVoList(@Param("limit") Integer limit);

    Long countPublishedByRating(@Param("tourId") Long tourId, @Param("rating") Integer rating);

    Double averagePublishedRating(@Param("tourId") Long tourId);

}
