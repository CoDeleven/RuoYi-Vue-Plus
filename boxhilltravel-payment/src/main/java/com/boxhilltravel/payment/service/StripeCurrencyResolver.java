package com.boxhilltravel.payment.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.domain.vo.SysDictDataVo;
import org.dromara.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Resolves the travel currency dictionary value to the ISO code required by Stripe.
 */
@RequiredArgsConstructor
@Service
public class StripeCurrencyResolver {

    private static final String CURRENCY_DICT_TYPE = "holidays_currency_unit";

    private final ISysDictTypeService dictTypeService;

    /**
     * Resolves an ISO code or a configured currency dictionary value.
     *
     * @param value ISO code, dictionary value, label, or remark
     * @return normalized lowercase ISO currency code
     */
    public String resolve(String value) {
        String trimmed = StringUtils.trim(value);
        if (StringUtils.isBlank(trimmed)) {
            throw new ServiceException("Payment currency is invalid");
        }
        if (trimmed.matches("[A-Za-z]{3}")) {
            return trimmed.toLowerCase(Locale.ROOT);
        }
        List<SysDictDataVo> currencies = dictTypeService.selectDictDataByType(CURRENCY_DICT_TYPE);
        return currencies.stream()
            .filter(item -> matches(item, trimmed))
            .map(this::findIsoCode)
            .filter(StringUtils::isNotBlank)
            .map(item -> item.toLowerCase(Locale.ROOT))
            .findFirst()
            .orElseThrow(() -> new ServiceException("Payment currency is invalid"));
    }

    private boolean matches(SysDictDataVo item, String value) {
        return value.equalsIgnoreCase(StringUtils.trim(item.getDictValue()))
            || value.equalsIgnoreCase(StringUtils.trim(item.getDictLabel()))
            || value.equalsIgnoreCase(StringUtils.trim(item.getRemark()));
    }

    private String findIsoCode(SysDictDataVo item) {
        for (String candidate : new String[]{item.getDictLabel(), item.getRemark(), item.getDictValue()}) {
            if (candidate != null && candidate.trim().matches("[A-Za-z]{3}")) {
                return candidate.trim();
            }
        }
        return null;
    }

}
