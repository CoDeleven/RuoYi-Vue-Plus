package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysDepartureBo;
import com.boxhilltravel.core.domain.vo.HolidaysDepartureVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 团期Service接口
 *
 * @author Lion Li
 * @date 2026-06-30 14:47:38
 */
public interface IHolidaysDepartureService {

    /**
     * 查询团期
     *
     * @param id 主键
     * @return 团期
     */
    HolidaysDepartureVo queryById(Long id);

    /**
     * 分页查询团期列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 团期分页列表
     */
    PageResult<HolidaysDepartureVo> queryPageList(HolidaysDepartureBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的团期列表
     *
     * @param bo 查询条件
     * @return 团期列表
     */
    List<HolidaysDepartureVo> queryList(HolidaysDepartureBo bo);


    /**
     * 新增团期
     *
     * @param bo 团期
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysDepartureBo bo);

    /**
     * 修改团期
     *
     * @param bo 团期
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysDepartureBo bo);



    /**
     * 校验并批量删除团期信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



