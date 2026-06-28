package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.vo.FaqGroupVo;

import java.util.List;

/**
 * Customer FAQ service.
 */
public interface IHolidaysFaqCustomerService {

    List<FaqGroupVo> queryFaqTree(Integer module);

}
