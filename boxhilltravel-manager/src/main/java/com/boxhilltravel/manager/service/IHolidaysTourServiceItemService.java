package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysTourServiceItemBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourServiceItemVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 线路服务项Service接口
 *
 * @author Lion Li
 * @date 2026-06-28 21:44:19
 */
public interface IHolidaysTourServiceItemService {

    /**
     * 查询线路服务项
     *
     * @param id 主键
     * @return 线路服务项
     */
    HolidaysTourServiceItemVo queryById(Long id);

    /**
     * 分页查询线路服务项列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 线路服务项分页列表
     */
    PageResult<HolidaysTourServiceItemVo> queryPageList(HolidaysTourServiceItemBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的线路服务项列表
     *
     * @param bo 查询条件
     * @return 线路服务项列表
     */
    List<HolidaysTourServiceItemVo> queryList(HolidaysTourServiceItemBo bo);


    /**
     * 新增线路服务项
     *
     * @param bo 线路服务项
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysTourServiceItemBo bo);

    /**
     * 修改线路服务项
     *
     * @param bo 线路服务项
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysTourServiceItemBo bo);



    /**
     * 校验并批量删除线路服务项信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



