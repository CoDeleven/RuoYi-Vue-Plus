package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.vo.TourListItemVo;

import java.util.List;

/**
 * Customer wishlist service.
 */
public interface ICustomerWishlistService {

    /**
     * Query current customer's wishlisted tours.
     *
     * @return wishlisted tours
     */
    List<TourListItemVo> list();

    /**
     * Query current customer's wishlisted tour ids.
     *
     * @return tour ids
     */
    List<Long> ids();

    /**
     * Check whether current customer has wishlisted a tour.
     *
     * @param tourId tour id
     * @return true if wishlisted
     */
    Boolean status(Long tourId);

    /**
     * Add a tour to current customer's wishlist.
     *
     * @param tourId tour id
     */
    void add(Long tourId);

    /**
     * Remove a tour from current customer's wishlist.
     *
     * @param tourId tour id
     */
    void remove(Long tourId);

}
