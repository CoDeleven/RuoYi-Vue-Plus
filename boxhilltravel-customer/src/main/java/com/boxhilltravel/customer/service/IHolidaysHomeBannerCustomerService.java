package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.vo.HomeBannerVo;

import java.util.List;

/**
 * Customer homepage banner service.
 */
public interface IHolidaysHomeBannerCustomerService {

    /**
     * Query enabled homepage hero banners, ordered by sort.
     *
     * @param limit max rows
     * @return banner list
     */
    List<HomeBannerVo> queryEnabledBanners(Integer limit);

}