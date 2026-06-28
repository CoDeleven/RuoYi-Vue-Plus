package com.boxhilltravel.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.customer.domain.vo.DepartureItemVo;
import com.boxhilltravel.customer.service.IHolidaysDepartureCustomerService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Customer departure service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysDepartureCustomerServiceImpl implements IHolidaysDepartureCustomerService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HolidaysDepartureMapper departureMapper;

    @Override
    public List<DepartureItemVo> queryDepartureListByTourId(Long tourId, String month) {
        var wrapper = Wrappers.lambdaQuery(HolidaysDeparture.class)
            .eq(HolidaysDeparture::getTourId, tourId)
            .orderByAsc(HolidaysDeparture::getDepartureDate)
            .orderByAsc(HolidaysDeparture::getId);
        if (StringUtils.isNotBlank(month)) {
            try {
                YearMonth yearMonth = YearMonth.parse(month);
                wrapper.ge(HolidaysDeparture::getDepartureDate, yearMonth.atDay(1))
                    .lt(HolidaysDeparture::getDepartureDate, yearMonth.plusMonths(1).atDay(1));
            } catch (DateTimeParseException e) {
                throw new ServiceException("Invalid month format, expected yyyy-MM");
            }
        }
        return departureMapper.selectList(wrapper).stream().map(this::toDepartureItemVo).toList();
    }

    private DepartureItemVo toDepartureItemVo(HolidaysDeparture departure) {
        DepartureItemVo vo = new DepartureItemVo();
        vo.setId(departure.getId());
        vo.setStartDate(departure.getDepartureDate() == null ? null : departure.getDepartureDate().format(DATE_FORMATTER));
        vo.setEndDate(departure.getReturnDate() == null ? null : departure.getReturnDate().format(DATE_FORMATTER));
        vo.setDuration(departure.getDurationDays() == null ? null : departure.getDurationDays().intValue());
        vo.setBasePrice(departure.getBasePrice());
        vo.setSalePrice(departure.getSalePrice());
        vo.setAvailableCount(departure.getAvailableCount() == null ? null : departure.getAvailableCount().intValue());
        vo.setStatus(departure.getStatus());
        return vo;
    }

}
