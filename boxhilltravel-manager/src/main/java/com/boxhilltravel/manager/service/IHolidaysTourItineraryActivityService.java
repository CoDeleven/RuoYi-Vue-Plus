package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysTourItineraryActivityBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourItineraryActivityVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 行程活动Service接口
 *
 * @author Lion Li
 * @date 2026-06-28 12:58:35
 */
public interface IHolidaysTourItineraryActivityService {

    /**
     * 查询行程活动
     *
     * @param id 主键
     * @return 行程活动
     */
    HolidaysTourItineraryActivityVo queryById(Long id);

    /**
     * 分页查询行程活动列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 行程活动分页列表
     */
    PageResult<HolidaysTourItineraryActivityVo> queryPageList(HolidaysTourItineraryActivityBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的行程活动列表
     *
     * @param bo 查询条件
     * @return 行程活动列表
     */
    List<HolidaysTourItineraryActivityVo> queryList(HolidaysTourItineraryActivityBo bo);


    /**
     * 新增行程活动
     *
     * @param bo 行程活动
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysTourItineraryActivityBo bo);

    /**
     * 修改行程活动
     *
     * @param bo 行程活动
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysTourItineraryActivityBo bo);



    /**
     * 校验并批量删除行程活动信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



