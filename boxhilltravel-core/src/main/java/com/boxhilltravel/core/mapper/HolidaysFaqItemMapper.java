package com.boxhilltravel.core.mapper;

import com.boxhilltravel.core.domain.HolidaysFaqItem;
import com.boxhilltravel.core.domain.bo.HolidaysFaqItemBo;
import com.boxhilltravel.core.domain.vo.HolidaysFaqItemVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * FAQ条目Mapper接口
 *
 * @author Lion Li
 * @date 2026-06-29 15:46:06
 */
public interface HolidaysFaqItemMapper extends BaseMapperPlus<HolidaysFaqItem, HolidaysFaqItemVo> {

    /**
     * 查询FAQ条目分页列表
     *
     * @param page 分页参数
     * @param bo 查询条件
     * @return FAQ条目分页列表
     */
    Page<HolidaysFaqItemVo> selectFaqItemVoPage(@Param("page") Page<HolidaysFaqItemVo> page, @Param("bo") HolidaysFaqItemBo bo);

    /**
     * 查询FAQ条目列表
     *
     * @param bo 查询条件
     * @return FAQ条目列表
     */
    List<HolidaysFaqItemVo> selectFaqItemVoList(@Param("bo") HolidaysFaqItemBo bo);

    /**
     * 查询FAQ条目详情
     *
     * @param id 主键
     * @return FAQ条目
     */
    HolidaysFaqItemVo selectFaqItemVoById(@Param("id") Long id);

}



