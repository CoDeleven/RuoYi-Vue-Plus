package com.boxhilltravel.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.CustomerProfile;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysReview;
import com.boxhilltravel.core.domain.HolidaysTour;
import com.boxhilltravel.core.domain.vo.HolidaysReviewVo;
import com.boxhilltravel.core.mapper.CustomerProfileMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysReviewMapper;
import com.boxhilltravel.core.mapper.HolidaysTourMapper;
import com.boxhilltravel.customer.domain.bo.CreateReviewBo;
import com.boxhilltravel.customer.domain.vo.ReviewItemVo;
import com.boxhilltravel.customer.domain.vo.ReviewSummaryVo;
import com.boxhilltravel.customer.service.IHolidaysReviewCustomerService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer review service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysReviewCustomerServiceImpl implements IHolidaysReviewCustomerService {

    private static final int SOURCE_CUSTOMER = 2;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int FEATURED_NO = 0;
    private static final List<Integer> REVIEWABLE_ORDER_STATUSES = List.of(3);

    private final HolidaysReviewMapper reviewMapper;
    private final HolidaysOrderMapper orderMapper;
    private final HolidaysTourMapper tourMapper;
    private final CustomerProfileMapper profileMapper;
    private final ISysUserService userService;

    @Override
    public ReviewSummaryVo queryTourReviews(Long tourId, PageQuery pageQuery) {
        if (tourId == null) {
            throw new ServiceException("Tour id is required");
        }
        Page<HolidaysReviewVo> page = reviewMapper.selectPublishedReviewVoPage(pageQuery.build(), tourId);
        ReviewSummaryVo vo = new ReviewSummaryVo();
        vo.setRows(page.getRecords().stream().map(this::toReviewItemVo).toList());
        vo.setTotal(page.getTotal());
        Double average = reviewMapper.averagePublishedRating(tourId);
        vo.setAverageRating(average == null ? 0D : average);
        Map<Integer, Long> ratingCounts = new LinkedHashMap<>();
        for (int rating = 5; rating >= 1; rating--) {
            ratingCounts.put(rating, reviewMapper.countPublishedByRating(tourId, rating));
        }
        vo.setRatingCounts(ratingCounts);
        return vo;
    }

    @Override
    public List<ReviewItemVo> queryHomeReviews(Integer limit) {
        int resolvedLimit = limit == null || limit <= 0 ? 6 : Math.min(limit, 20);
        return reviewMapper.selectHomeReviewVoList(resolvedLimit).stream().map(this::toReviewItemVo).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewItemVo create(CreateReviewBo bo) {
        Long userId = LoginHelper.getUserId();
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("User not found");
        }
        HolidaysOrder order = orderMapper.selectById(bo.getOrderId());
        if (order == null || !userId.equals(order.getCustomerUserId())) {
            throw new ServiceException("Booking not found");
        }
        if (!bo.getTourId().equals(order.getTourId())) {
            throw new ServiceException("Booking does not match this tour");
        }
        if (!REVIEWABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new ServiceException("Only completed bookings can be reviewed");
        }
        HolidaysTour tour = tourMapper.selectById(order.getTourId());
        if (tour == null || !Long.valueOf(1L).equals(tour.getStatus()) || tour.getDeletedAt() != null) {
            throw new ServiceException("Tour not found");
        }
        Long existingCount = reviewMapper.selectCount(Wrappers.lambdaQuery(HolidaysReview.class)
            .eq(HolidaysReview::getCustomerUserId, userId)
            .eq(HolidaysReview::getTourId, order.getTourId()));
        if (existingCount != null && existingCount > 0) {
            throw new ServiceException("You have already reviewed this tour");
        }

        CustomerProfile profile = profileMapper.selectById(userId);
        HolidaysReview review = new HolidaysReview();
        review.setTourId(order.getTourId());
        review.setOrderId(order.getId());
        review.setCustomerUserId(userId);
        review.setNickname(resolveNickname(profile, user));
        review.setAvatar(resolveAvatar(profile, user));
        review.setTitle(StringUtils.trim(bo.getTitle()));
        review.setContent(StringUtils.trim(bo.getContent()));
        review.setRating(bo.getRating());
        review.setSource(SOURCE_CUSTOMER);
        review.setStatus(STATUS_PENDING);
        review.setFeatured(FEATURED_NO);
        review.setSortOrder(0);
        validateReview(review);
        reviewMapper.insert(review);

        HolidaysReviewVo saved = reviewMapper.selectReviewVoById(review.getId());
        return toReviewItemVo(saved);
    }

    private ReviewItemVo    toReviewItemVo(HolidaysReviewVo review) {
        ReviewItemVo vo = new ReviewItemVo();
        vo.setId(review.getId());
        vo.setTourId(review.getTourId());
        vo.setTourName(review.getTourName());
        vo.setUserId(review.getCustomerUserId());
        vo.setUserName(review.getNickname());
        vo.setAvatar(review.getAvatar());
        vo.setRating(review.getRating());
        vo.setTitle(review.getTitle());
        vo.setContent(review.getContent());
        vo.setCreatedAt(review.getPublishedAt() == null ? review.getCreateTime() : review.getPublishedAt());
        vo.setIsVerified(review.getOrderId() != null && review.getCustomerUserId() != null);
        return vo;
    }

    private String resolveNickname(CustomerProfile profile, SysUserVo user) {
        if (profile != null && StringUtils.isNotBlank(profile.getNickname())) {
            return StringUtils.trim(profile.getNickname());
        }
        if (StringUtils.isNotBlank(user.getNickName())) {
            return StringUtils.trim(user.getNickName());
        }
        return user.getUserName();
    }

    private String resolveAvatar(CustomerProfile profile, SysUserVo user) {
        if (profile != null && StringUtils.isNotBlank(profile.getAvatarUrl())) {
            return StringUtils.trim(profile.getAvatarUrl());
        }
        return user.getAvatar() == null ? null : String.valueOf(user.getAvatar());
    }

    private void validateReview(HolidaysReview review) {
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new ServiceException("Rating must be between 1 and 5");
        }
        if (StringUtils.isBlank(review.getTitle())) {
            throw new ServiceException("Title is required");
        }
        if (StringUtils.isBlank(review.getContent())) {
            throw new ServiceException("Content is required");
        }
        review.setPublishedAt(null);
        review.setRejectReason(null);
    }

}
