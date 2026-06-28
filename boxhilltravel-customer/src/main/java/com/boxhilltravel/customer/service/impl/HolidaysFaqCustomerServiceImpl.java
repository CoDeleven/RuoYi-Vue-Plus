package com.boxhilltravel.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.constant.HolidaysFaqConstants;
import com.boxhilltravel.core.domain.HolidaysFaqGroup;
import com.boxhilltravel.core.domain.HolidaysFaqItem;
import com.boxhilltravel.core.mapper.HolidaysFaqGroupMapper;
import com.boxhilltravel.core.mapper.HolidaysFaqItemMapper;
import com.boxhilltravel.customer.domain.vo.FaqGroupVo;
import com.boxhilltravel.customer.domain.vo.FaqItemVo;
import com.boxhilltravel.customer.service.IHolidaysFaqCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer FAQ service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysFaqCustomerServiceImpl implements IHolidaysFaqCustomerService {

    private static final String STATUS_ENABLED = "1";

    private final HolidaysFaqGroupMapper faqGroupMapper;
    private final HolidaysFaqItemMapper faqItemMapper;

    @Override
    public List<FaqGroupVo> queryFaqTree(Integer module) {
        Integer faqModule = ObjectUtil.defaultIfNull(module, HolidaysFaqConstants.DEFAULT_MODULE);
        List<HolidaysFaqGroup> groups = faqGroupMapper.selectList(Wrappers.lambdaQuery(HolidaysFaqGroup.class)
            .eq(HolidaysFaqGroup::getModule, faqModule)
            .eq(HolidaysFaqGroup::getStatus, STATUS_ENABLED)
            .orderByAsc(HolidaysFaqGroup::getSortOrder)
            .orderByAsc(HolidaysFaqGroup::getId));
        if (groups.isEmpty()) {
            return List.of();
        }
        Map<Long, FaqGroupVo> groupMap = new LinkedHashMap<>();
        for (HolidaysFaqGroup group : groups) {
            FaqGroupVo vo = new FaqGroupVo();
            vo.setId(group.getId());
            vo.setName(group.getGroupName());
            vo.setModule(group.getModule());
            vo.setIcon(group.getIcon());
            vo.setSortOrder(group.getSortOrder());
            vo.setItems(new ArrayList<>());
            groupMap.put(group.getId(), vo);
        }
        List<HolidaysFaqItem> items = faqItemMapper.selectList(Wrappers.lambdaQuery(HolidaysFaqItem.class)
            .in(HolidaysFaqItem::getGroupId, groupMap.keySet())
            .eq(HolidaysFaqItem::getModule, faqModule)
            .eq(HolidaysFaqItem::getStatus, STATUS_ENABLED)
            .orderByAsc(HolidaysFaqItem::getGroupId)
            .orderByAsc(HolidaysFaqItem::getSortOrder)
            .orderByAsc(HolidaysFaqItem::getId));
        for (HolidaysFaqItem item : items) {
            FaqGroupVo group = groupMap.get(item.getGroupId());
            if (group == null) {
                continue;
            }
            FaqItemVo itemVo = new FaqItemVo();
            itemVo.setId(item.getId());
            itemVo.setGroupId(item.getGroupId());
            itemVo.setModule(item.getModule());
            itemVo.setQuestion(item.getQuestion());
            itemVo.setAnswer(item.getAnswer());
            itemVo.setSortOrder(item.getSortOrder());
            group.getItems().add(itemVo);
        }
        return new ArrayList<>(groupMap.values());
    }

}
