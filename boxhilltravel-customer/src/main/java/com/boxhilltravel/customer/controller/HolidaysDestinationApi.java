package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.customer.domain.vo.DestinationVo;
import com.boxhilltravel.customer.domain.vo.PopularDestinationVo;
import com.boxhilltravel.customer.service.IHolidaysDestinationCustomerService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Customer destination API.
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/destination")
public class HolidaysDestinationApi extends BaseController {

    private final IHolidaysDestinationCustomerService destinationCustomerService;

    @GetMapping("/tree")
    public R<List<DestinationVo>> tree() {
        return R.ok(destinationCustomerService.queryDestinationTree());
    }

    @GetMapping("/children/{parentId}")
    public R<List<DestinationVo>> children(@NotNull(message = "Parent id is required") @PathVariable Long parentId) {
        return R.ok(destinationCustomerService.queryChildrenByParentId(parentId));
    }

    @GetMapping("/available_tree")
    public R<List<DestinationVo>> availableTree(@RequestParam(required = false) String destinationNameEn) {
        return R.ok(destinationCustomerService.queryAvailableTree(destinationNameEn));
    }

    @GetMapping("/query_destination")
    public R<DestinationVo> queryDestination(@NotBlank(message = "Destination name is required") @RequestParam String destinationNameEn) {
        return R.ok(destinationCustomerService.queryDestination(destinationNameEn));
    }

    @GetMapping("/popular")
    public R<List<PopularDestinationVo>> popular(@RequestParam(required = false) Integer limit) {
        return R.ok(destinationCustomerService.queryPopularDestinations(limit));
    }

    @GetMapping("/featured")
    public R<List<DestinationVo>> featured() {
        return R.ok(destinationCustomerService.queryFeaturedDestinations());
    }

    @GetMapping("/tour_counts")
    public R<Map<Long, Long>> tourCounts(@RequestParam(required = false) List<Long> countryIds) {
        return R.ok(destinationCustomerService.queryTourCounts(countryIds));
    }

}
