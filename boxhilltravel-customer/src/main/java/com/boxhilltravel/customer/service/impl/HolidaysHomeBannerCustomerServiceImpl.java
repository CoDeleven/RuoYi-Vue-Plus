package com.boxhilltravel.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysHomeBanner;
import com.boxhilltravel.core.mapper.HolidaysHomeBannerMapper;
import com.boxhilltravel.customer.domain.vo.HomeBannerVo;
import com.boxhilltravel.customer.service.IHolidaysHomeBannerCustomerService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Customer homepage banner service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysHomeBannerCustomerServiceImpl implements IHolidaysHomeBannerCustomerService {

    private final HolidaysHomeBannerMapper homeBannerMapper;

    @Override
    public List<HomeBannerVo> queryEnabledBanners(Integer limit) {
        int resolvedLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 10);
        return homeBannerMapper.selectList(Wrappers.lambdaQuery(HolidaysHomeBanner.class)
                .eq(HolidaysHomeBanner::getStatus, 1L)
                .orderByAsc(HolidaysHomeBanner::getSortOrder)
                .orderByAsc(HolidaysHomeBanner::getId)
                .last("limit " + resolvedLimit))
            .stream()
            .filter(banner -> StringUtils.isNotBlank(banner.getImage()))
            .map(this::toHomeBannerVo)
            .toList();
    }

    private HomeBannerVo toHomeBannerVo(HolidaysHomeBanner banner) {
        HomeBannerVo vo = new HomeBannerVo();
        vo.setId(banner.getId());
        vo.setImage(banner.getImage());
        vo.setAltText(banner.getAltText());
        vo.setTitle(banner.getTitle());
        vo.setSubtitle(banner.getSubtitle());
        vo.setLinkUrl(banner.getLinkUrl());
        return vo;
    }

}