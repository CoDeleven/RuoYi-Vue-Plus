package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysCustomerWishlist;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer wishlist view object.
 */
@Data
@AutoMapper(target = HolidaysCustomerWishlist.class)
public class HolidaysCustomerWishlistVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long customerUserId;

    private Long tourId;

    private LocalDateTime createTime;

}
