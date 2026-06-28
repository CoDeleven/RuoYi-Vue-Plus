package com.boxhilltravel.manager.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysReview;
import com.boxhilltravel.core.domain.bo.HolidaysReviewBo;
import com.boxhilltravel.core.domain.vo.HolidaysReviewVo;
import com.boxhilltravel.core.mapper.HolidaysReviewMapper;
import com.boxhilltravel.manager.service.IHolidaysReviewService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Tour review manager service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysReviewServiceImpl implements IHolidaysReviewService {

    private static final int SOURCE_MANAGER = 1;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_REJECTED = 2;
    private static final int FEATURED_NO = 0;

    private final HolidaysReviewMapper holidaysReviewMapper;

    @Override
    public HolidaysReviewVo queryById(Long id) {
        HolidaysReviewVo vo = holidaysReviewMapper.selectReviewVoById(id);
        if (vo == null) {
            throw new ServiceException("Review not found");
        }
        return vo;
    }

    @Override
    public PageResult<HolidaysReviewVo> queryPageList(HolidaysReviewBo bo, PageQuery pageQuery) {
        Page<HolidaysReviewVo> result = holidaysReviewMapper.selectReviewVoPage(pageQuery.build(), bo);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public Boolean insertByBo(HolidaysReviewBo bo) {
        HolidaysReview add = MapstructUtils.convert(bo, HolidaysReview.class);
        if (add == null) {
            throw new ServiceException("Review request is required");
        }
        if (add.getSource() == null) {
            add.setSource(SOURCE_MANAGER);
        }
        if (add.getStatus() == null) {
            add.setStatus(STATUS_PUBLISHED);
        }
        if (add.getFeatured() == null) {
            add.setFeatured(FEATURED_NO);
        }
        if (add.getSortOrder() == null) {
            add.setSortOrder(0);
        }
        normalizeBeforeSave(add, null);
        boolean flag = holidaysReviewMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(HolidaysReviewBo bo) {
        HolidaysReview update = MapstructUtils.convert(bo, HolidaysReview.class);
        if (update == null || update.getId() == null) {
            throw new ServiceException("Review id is required");
        }
        HolidaysReview existing = holidaysReviewMapper.selectById(update.getId());
        if (existing == null) {
            throw new ServiceException("Review not found");
        }
        normalizeBeforeSave(update, existing);
        return holidaysReviewMapper.updateById(update) > 0;
    }

    @Override
    public Boolean audit(Long id, Integer status, String rejectReason) {
        if (id == null) {
            throw new ServiceException("Review id is required");
        }
        if (!Integer.valueOf(STATUS_PUBLISHED).equals(status) && !Integer.valueOf(STATUS_REJECTED).equals(status)) {
            throw new ServiceException("Unsupported review audit status");
        }
        var update = Wrappers.lambdaUpdate(HolidaysReview.class)
            .eq(HolidaysReview::getId, id)
            .set(HolidaysReview::getStatus, status)
            .set(HolidaysReview::getRejectReason, Integer.valueOf(STATUS_REJECTED).equals(status) ? StringUtils.trim(rejectReason) : null);
        if (Integer.valueOf(STATUS_PUBLISHED).equals(status)) {
            update.set(HolidaysReview::getPublishedAt, LocalDateTime.now());
        } else {
            update.set(HolidaysReview::getFeatured, FEATURED_NO);
        }
        return holidaysReviewMapper.update(null, update) > 0;
    }

    @Override
    public Boolean updateFeatured(Long id, Integer featured) {
        if (id == null) {
            throw new ServiceException("Review id is required");
        }
        int normalizedFeatured = Integer.valueOf(1).equals(featured) ? 1 : FEATURED_NO;
        return holidaysReviewMapper.update(null, Wrappers.lambdaUpdate(HolidaysReview.class)
            .eq(HolidaysReview::getId, id)
            .eq(HolidaysReview::getStatus, STATUS_PUBLISHED)
            .set(HolidaysReview::getFeatured, normalizedFeatured)) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return holidaysReviewMapper.deleteByIds(ids) > 0;
    }

    private void normalizeBeforeSave(HolidaysReview review, HolidaysReview existing) {
        validateRating(review.getRating());
        review.setNickname(StringUtils.trim(review.getNickname()));
        review.setAvatar(StringUtils.trim(review.getAvatar()));
        review.setTitle(StringUtils.trim(review.getTitle()));
        review.setContent(StringUtils.trim(review.getContent()));
        review.setRejectReason(StringUtils.trim(review.getRejectReason()));
        if (review.getStatus() != null && !Integer.valueOf(STATUS_PENDING).equals(review.getStatus())
            && !Integer.valueOf(STATUS_PUBLISHED).equals(review.getStatus()) && !Integer.valueOf(STATUS_REJECTED).equals(review.getStatus())) {
            throw new ServiceException("Unsupported review status");
        }
        if (Integer.valueOf(STATUS_PUBLISHED).equals(review.getStatus()) && review.getPublishedAt() == null && existing != null && existing.getPublishedAt() != null) {
            review.setPublishedAt(existing.getPublishedAt());
        } else if (Integer.valueOf(STATUS_PUBLISHED).equals(review.getStatus()) && review.getPublishedAt() == null) {
            review.setPublishedAt(LocalDateTime.now());
        }
        if (Integer.valueOf(STATUS_REJECTED).equals(review.getStatus())) {
            review.setFeatured(FEATURED_NO);
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ServiceException("Rating must be between 1 and 5");
        }
    }

}
