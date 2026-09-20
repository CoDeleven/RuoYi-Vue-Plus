package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Customer application config.
 */
@Data
public class AppConfigVo {

    private Map<String, List<EnumItemVo>> enums;

    private Map<String, List<SortedItemVo>> sortTypes;

    private Boolean cskhChatEnabled;

}
