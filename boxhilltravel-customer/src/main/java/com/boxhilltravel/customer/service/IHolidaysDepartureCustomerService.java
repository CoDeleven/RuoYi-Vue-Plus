package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.vo.DepartureItemVo;

import java.util.List;

/**
 * Customer departure service.
 */
public interface IHolidaysDepartureCustomerService {

    List<DepartureItemVo> queryDepartureListByTourId(Long tourId, String month);

}
