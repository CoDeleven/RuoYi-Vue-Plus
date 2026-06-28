package com.boxhilltravel.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysOrderExtra;
import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import com.boxhilltravel.core.domain.HolidaysOrderTraveler;
import com.boxhilltravel.core.domain.vo.HolidaysOrderExtraVo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderPaymentVo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderTravelerVo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderVo;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderExtraMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderPaymentMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderTravelerMapper;
import com.boxhilltravel.customer.domain.bo.CheckoutQuoteBo;
import com.boxhilltravel.customer.domain.bo.CreateOrderBo;
import com.boxhilltravel.customer.domain.vo.OrderListItemVo;
import com.boxhilltravel.customer.service.ICustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Customer order service implementation.
 */
@RequiredArgsConstructor
@Service
public class CustomerOrderServiceImpl implements ICustomerOrderService {

    private static final Integer ORDER_STATUS_PENDING_PAYMENT = 0;
    private static final Integer ORDER_STATUS_PAID = 1;
    private static final String PAYMENT_STATUS_PENDING = "PENDING";
    private static final String PAYMENT_STATUS_PAID = "PAID";
    private static final String PAYMENT_METHOD_PLACEHOLDER = "PLACEHOLDER";
    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CheckoutServiceImpl checkoutService;
    private final ISysUserService userService;
    private final HolidaysOrderMapper orderMapper;
    private final HolidaysOrderTravelerMapper travelerMapper;
    private final HolidaysOrderExtraMapper extraMapper;
    private final HolidaysOrderPaymentMapper paymentMapper;
    private final HolidaysDepartureMapper departureMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HolidaysOrderVo create(CreateOrderBo bo) {
        Long userId = LoginHelper.getUserId();
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("User not found");
        }
        validateCreateOrder(bo);
        CheckoutServiceImpl.QuoteContext quote = checkoutService.buildQuoteContext(bo);
        reserveDepartureSeats(quote);

        HolidaysOrder order = new HolidaysOrder();
        order.setOrderNo(generateOrderNo());
        order.setCustomerUserId(userId);
        order.setCustomerUsername(user.getUserName());
        order.setCustomerEmail(StringUtils.isNotBlank(user.getEmail()) ? user.getEmail() : user.getUserName());
        order.setTourId(quote.tour().getId());
        order.setTourCode(quote.tour().getCode());
        order.setTourName(quote.tour().getName());
        order.setTourCoverImage(quote.tour().getCoverImage());
        order.setDepartureId(quote.departure().getId());
        order.setDepartureDate(quote.departure().getDepartureDate());
        order.setReturnDate(quote.departure().getReturnDate());
        order.setTravelerCount(quote.travelerCount());
        order.setCurrency(quote.currency());
        order.setUnitPrice(quote.unitPrice());
        order.setTourAmount(quote.tourAmount());
        order.setExtrasAmount(quote.extrasAmount());
        order.setTotalAmount(quote.tourAmount().add(quote.extrasAmount()));
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus(ORDER_STATUS_PENDING_PAYMENT);
        fillContact(order, bo.getContact());
        order.setTermsAccepted(bo.getTermsAccepted());
        order.setRemark(StringUtils.trim(bo.getRemark()));
        orderMapper.insert(order);

        insertTravelers(order.getId(), bo.getTravelers());
        insertExtras(order.getId(), bo.getTravelExtras(), quote.departure().getDepartureDate(), quote.departure().getReturnDate());
        insertPayment(order);
        return queryById(order.getId());
    }

    @Override
    public HolidaysOrderVo queryById(Long id) {
        Long userId = LoginHelper.getUserId();
        HolidaysOrderVo vo = orderMapper.selectVoOne(Wrappers.lambdaQuery(HolidaysOrder.class)
            .eq(HolidaysOrder::getId, id)
            .eq(HolidaysOrder::getCustomerUserId, userId));
        if (vo == null) {
            throw new ServiceException("Order not found");
        }
        fillChildren(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HolidaysOrderVo pay(Long id) {
        Long userId = LoginHelper.getUserId();
        HolidaysOrder order = orderMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrder.class)
            .eq(HolidaysOrder::getId, id)
            .eq(HolidaysOrder::getCustomerUserId, userId));
        if (order == null) {
            throw new ServiceException("Order not found");
        }
        if (ORDER_STATUS_PAID.equals(order.getStatus())) {
            return queryById(id);
        }
        if (!ORDER_STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new ServiceException("Only pending payment orders can be paid");
        }

        LocalDateTime paidTime = LocalDateTime.now();
        int updated = orderMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrder.class)
            .set(HolidaysOrder::getStatus, ORDER_STATUS_PAID)
            .set(HolidaysOrder::getPaidAmount, order.getTotalAmount())
            .eq(HolidaysOrder::getId, id)
            .eq(HolidaysOrder::getCustomerUserId, userId)
            .eq(HolidaysOrder::getStatus, ORDER_STATUS_PENDING_PAYMENT));
        if (updated == 0) {
            throw new ServiceException("Order payment status has changed");
        }

        paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
            .set(HolidaysOrderPayment::getStatus, PAYMENT_STATUS_PAID)
            .set(HolidaysOrderPayment::getPaidTime, paidTime)
            .eq(HolidaysOrderPayment::getOrderId, id)
            .eq(HolidaysOrderPayment::getStatus, PAYMENT_STATUS_PENDING));
        return queryById(id);
    }

    @Override
    public PageResult<OrderListItemVo> queryMyOrders(PageQuery pageQuery, Integer status) {
        Page<HolidaysOrder> page = orderMapper.selectPage(pageQuery.build(), Wrappers.lambdaQuery(HolidaysOrder.class)
            .eq(HolidaysOrder::getCustomerUserId, LoginHelper.getUserId())
            .eq(status != null, HolidaysOrder::getStatus, status)
            .orderByDesc(HolidaysOrder::getCreateTime)
            .orderByDesc(HolidaysOrder::getId));
        List<OrderListItemVo> rows = page.getRecords().stream().map(this::toListItemVo).toList();
        return PageResult.build(rows, page.getTotal());
    }

    private void validateCreateOrder(CreateOrderBo bo) {
        if (bo == null) {
            throw new ServiceException("Order request is required");
        }
        if (!Boolean.TRUE.equals(bo.getTermsAccepted())) {
            throw new ServiceException("Please accept the terms before booking");
        }
        if (bo.getTravelers() == null || bo.getTravelers().size() != bo.getTravelerCount()) {
            throw new ServiceException("Traveler count does not match traveler information");
        }
    }

    private void reserveDepartureSeats(CheckoutServiceImpl.QuoteContext quote) {
        int travelerCount = quote.travelerCount();
        int updated = departureMapper.update(null, Wrappers.lambdaUpdate(HolidaysDeparture.class)
            .setSql("booked_count = COALESCE(booked_count, 0) + {0}", travelerCount)
            .setSql("available_count = available_count - {0}", travelerCount)
            .eq(HolidaysDeparture::getId, quote.departure().getId())
            .eq(HolidaysDeparture::getTourId, quote.tour().getId())
            .eq(HolidaysDeparture::getStatus, 1)
            .isNotNull(HolidaysDeparture::getAvailableCount)
            .ge(HolidaysDeparture::getAvailableCount, travelerCount));
        if (updated == 0) {
            throw new ServiceException("Not enough seats available");
        }

        HolidaysDeparture latest = departureMapper.selectById(quote.departure().getId());
        if (latest != null && Long.valueOf(0L).equals(latest.getAvailableCount())) {
            departureMapper.update(null, Wrappers.lambdaUpdate(HolidaysDeparture.class)
                .set(HolidaysDeparture::getStatus, 2)
                .eq(HolidaysDeparture::getId, quote.departure().getId())
                .eq(HolidaysDeparture::getStatus, 1)
                .eq(HolidaysDeparture::getAvailableCount, 0));
        }
    }

    private void fillContact(HolidaysOrder order, CreateOrderBo.ContactBo contact) {
        order.setContactName(StringUtils.trim(contact.getName()));
        order.setContactEmail(StringUtils.trim(contact.getEmail()));
        order.setContactPhone(StringUtils.trim(contact.getPhone()));
        order.setContactAddress(StringUtils.trim(contact.getAddress()));
        order.setContactCity(StringUtils.trim(contact.getCity()));
        order.setContactRegion(StringUtils.trim(contact.getRegion()));
        order.setContactPostalCode(StringUtils.trim(contact.getPostalCode()));
        order.setContactCountry(StringUtils.trim(contact.getCountry()));
    }

    private void insertTravelers(Long orderId, List<CreateOrderBo.TravelerBo> travelers) {
        int travelerNo = 1;
        for (CreateOrderBo.TravelerBo item : travelers) {
            HolidaysOrderTraveler traveler = new HolidaysOrderTraveler();
            traveler.setOrderId(orderId);
            traveler.setTravelerNo(travelerNo);
            traveler.setPrimaryTraveler(Boolean.TRUE.equals(item.getPrimaryTraveler()) || travelerNo == 1);
            traveler.setTitle(StringUtils.trim(item.getTitle()));
            traveler.setFirstName(StringUtils.trim(item.getFirstName()));
            traveler.setMiddleName(StringUtils.trim(item.getMiddleName()));
            traveler.setLastName(StringUtils.trim(item.getLastName()));
            traveler.setNoMiddleName(item.getNoMiddleName());
            traveler.setDateOfBirth(item.getDateOfBirth());
            traveler.setEmail(StringUtils.trim(item.getEmail()));
            traveler.setPhone(StringUtils.trim(item.getPhone()));
            traveler.setPlaceOfBirth(StringUtils.trim(item.getPlaceOfBirth()));
            traveler.setNationality(StringUtils.trim(item.getNationality()));
            traveler.setPassportNumber(StringUtils.trim(item.getPassportNumber()));
            traveler.setPassportExpiryDate(item.getPassportExpiryDate());
            traveler.setAddress(StringUtils.trim(item.getAddress()));
            traveler.setCity(StringUtils.trim(item.getCity()));
            traveler.setRegion(StringUtils.trim(item.getRegion()));
            traveler.setPostalCode(StringUtils.trim(item.getPostalCode()));
            traveler.setCountry(StringUtils.trim(item.getCountry()));
            travelerMapper.insert(traveler);
            travelerNo++;
        }
    }

    private void insertExtras(Long orderId, CheckoutQuoteBo.TravelExtrasBo travelExtras, LocalDate departureDate, LocalDate returnDate) {
        if (departureDate == null || returnDate == null) {
            return;
        }
        LocalDate arriveDate = travelExtras == null || travelExtras.getArriveDate() == null ? departureDate : travelExtras.getArriveDate();
        LocalDate departDate = travelExtras == null || travelExtras.getDepartDate() == null ? returnDate : travelExtras.getDepartDate();
        if (arriveDate.isAfter(departureDate)) {
            throw new ServiceException("Arrive date cannot be later than the tour departure date");
        }
        if (departDate.isBefore(returnDate)) {
            throw new ServiceException("Depart date cannot be earlier than the tour return date");
        }
        if (arriveDate.isBefore(departureDate)) {
            insertTravelRequest(orderId, "TRAVEL_ARRIVE_EARLIER", "Arrive earlier", departureDate, arriveDate);
        }
        if (departDate.isAfter(returnDate)) {
            insertTravelRequest(orderId, "TRAVEL_DEPART_LATER", "Depart later", returnDate, departDate);
        }
    }

    private void insertTravelRequest(Long orderId, String type, String title, LocalDate baseDate, LocalDate requestedDate) {
        HolidaysOrderExtra extra = new HolidaysOrderExtra();
        extra.setOrderId(orderId);
        extra.setExtraType(type);
        extra.setTitle(title);
        extra.setAmount(BigDecimal.ZERO);
        extra.setQuantity(1);
        extra.setMetadata(JsonUtils.toJsonString(Map.of(
            "baseDate", baseDate,
            "requestedDate", requestedDate
        )));
        extraMapper.insert(extra);
    }

    private void insertPayment(HolidaysOrder order) {
        HolidaysOrderPayment payment = new HolidaysOrderPayment();
        payment.setOrderId(order.getId());
        payment.setPaymentNo("PAY" + order.getOrderNo());
        payment.setPaymentMethod(PAYMENT_METHOD_PLACEHOLDER);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());
        payment.setStatus(PAYMENT_STATUS_PENDING);
        paymentMapper.insert(payment);
    }

    private void fillChildren(HolidaysOrderVo vo) {
        List<HolidaysOrderTravelerVo> travelers = travelerMapper.selectVoList(Wrappers.lambdaQuery(HolidaysOrderTraveler.class)
            .eq(HolidaysOrderTraveler::getOrderId, vo.getId())
            .orderByAsc(HolidaysOrderTraveler::getTravelerNo)
            .orderByAsc(HolidaysOrderTraveler::getId));
        List<HolidaysOrderExtraVo> extras = extraMapper.selectVoList(Wrappers.lambdaQuery(HolidaysOrderExtra.class)
            .eq(HolidaysOrderExtra::getOrderId, vo.getId())
            .orderByAsc(HolidaysOrderExtra::getId));
        HolidaysOrderPaymentVo payment = paymentMapper.selectVoOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getOrderId, vo.getId())
            .orderByAsc(HolidaysOrderPayment::getId)
            .last("limit 1"));
        vo.setTravelers(travelers);
        vo.setExtras(extras);
        vo.setPayment(payment);
    }

    private OrderListItemVo toListItemVo(HolidaysOrder order) {
        OrderListItemVo vo = new OrderListItemVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTourId(order.getTourId());
        vo.setTourName(order.getTourName());
        vo.setTourCoverImage(order.getTourCoverImage());
        vo.setDepartureDate(order.getDepartureDate());
        vo.setTravelerCount(order.getTravelerCount());
        vo.setCurrency(order.getCurrency());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private String generateOrderNo() {
        return "BH" + LocalDateTime.now().format(ORDER_NO_TIME) + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

}
