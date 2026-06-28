package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.vo.HolidaysTourItineraryVo;
import com.boxhilltravel.core.domain.bo.HolidaysTourItineraryBo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 行程Service接口
 *
 * @author Lion Li
 * @date 2026-06-27 15:20:48
 */
public interface IHolidaysTourItineraryService {

    /**
     * 查询行程
     *
     * @param id 主键
     * @return 行程
     */
    HolidaysTourItineraryVo queryById(Long id);

    /**
     * 分页查询行程列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 行程分页列表
     */
    PageResult<HolidaysTourItineraryVo> queryPageList(HolidaysTourItineraryBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的行程列表
     *
     * @param bo 查询条件
     * @return 行程列表
     */
    List<HolidaysTourItineraryVo> queryList(HolidaysTourItineraryBo bo);


    /**
     * 新增行程
     *
     * @param bo 行程
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysTourItineraryBo bo);

    /**
     * 修改行程
     *
     * @param bo 行程
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysTourItineraryBo bo);



    /**
     * 校验并批量删除行程信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



