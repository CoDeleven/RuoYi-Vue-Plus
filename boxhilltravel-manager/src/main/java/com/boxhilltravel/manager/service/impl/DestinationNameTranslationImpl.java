package com.boxhilltravel.manager.service.impl;

import com.boxhilltravel.core.domain.vo.HolidaysDestinationVo;
import com.boxhilltravel.manager.service.IHolidaysDestinationService;
import lombok.AllArgsConstructor;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.common.translation.core.TranslationInterface;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author CoDeleven
 * @since 2026/6/27
 */
@Service
@AllArgsConstructor
@TranslationType(type = TransConstant.DESTINATION_ID_TO_NAME)
public class DestinationNameTranslationImpl implements TranslationInterface<String> {

    private final IHolidaysDestinationService destinationService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof String ids) {
            List<HolidaysDestinationVo> voList = destinationService.queryByIds(ids);
            return voList.stream().map(HolidaysDestinationVo::getNameEn).collect(Collectors.joining(","));
        } else if (key instanceof Long id) {
            HolidaysDestinationVo vo = destinationService.queryById(id);
            return Optional.ofNullable(vo).map(HolidaysDestinationVo::getNameEn).orElse("");
        }
        return "";
    }
}
