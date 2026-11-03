package com.boxhilltravel.core.mapper;

import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.vo.HolidaysOrderVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Order mapper.
 */
public interface HolidaysOrderMapper extends BaseMapperPlus<HolidaysOrder, HolidaysOrderVo> {

    /**
     * Locks an order while its reservation, payment, or refund state is changed.
     * Must be called inside a transaction.
     *
     * @param id order ID
     * @return locked order, or null
     */
    default HolidaysOrder selectByIdForUpdate(Long id) {
        return selectOne(Wrappers.lambdaQuery(HolidaysOrder.class)
            .eq(HolidaysOrder::getId, id).last("FOR UPDATE"));
    }
}
