package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 线路管理Service接口
 *
 * @author CoDeleven
 * @date 2026-06-26 18:26:30
 */
public interface IHolidaysTourService {

    /**
     * 查询线路管理
     *
     * @param id 主键
     * @return 线路管理
     */
    HolidaysTourVo queryById(Long id);

    /**
     * 分页查询线路管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 线路管理分页列表
     */
    PageResult<HolidaysTourVo> queryPageList(HolidaysTourBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的线路管理列表
     *
     * @param bo 查询条件
     * @return 线路管理列表
     */
    List<HolidaysTourVo> queryList(HolidaysTourBo bo);


    /**
     * 新增线路管理
     *
     * @param bo 线路管理
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysTourBo bo);

    /**
     * 修改线路管理
     *
     * @param bo 线路管理
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysTourBo bo);

    /**
     * 修改线路管理状态
     *
     * @param id 主键
     * @param status 状态值
     * @return 是否修改成功
     */
    Boolean updateStatus(Long id, Long status);


    /**
     * 校验并批量删除线路管理信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



