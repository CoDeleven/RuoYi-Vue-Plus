package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysFeaturedTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysFeaturedTourVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 精选线路Service接口
 *
 * @author Lion Li
 * @date 2026-06-29 00:06:41
 */
public interface IHolidaysFeaturedTourService {

    /**
     * 查询精选线路
     *
     * @param id 主键
     * @return 精选线路
     */
    HolidaysFeaturedTourVo queryById(Long id);

    /**
     * 分页查询精选线路列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 精选线路分页列表
     */
    PageResult<HolidaysFeaturedTourVo> queryPageList(HolidaysFeaturedTourBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的精选线路列表
     *
     * @param bo 查询条件
     * @return 精选线路列表
     */
    List<HolidaysFeaturedTourVo> queryList(HolidaysFeaturedTourBo bo);


    /**
     * 新增精选线路
     *
     * @param bo 精选线路
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysFeaturedTourBo bo);

    /**
     * 修改精选线路
     *
     * @param bo 精选线路
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysFeaturedTourBo bo);


    /**
     * 调整精选线路排序
     *
     * @param id 主键
     * @param sortValue 排序值
     * @return 是否修改成功
     */
    Boolean updateSort(Long id, Integer sortValue);

    /**
     * 校验并批量删除精选线路信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



