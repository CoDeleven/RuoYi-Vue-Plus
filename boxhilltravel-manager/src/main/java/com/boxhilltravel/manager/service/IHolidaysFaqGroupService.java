package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysFaqGroupBo;
import com.boxhilltravel.core.domain.vo.HolidaysFaqGroupVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * FAQ分组Service接口
 *
 * @author Lion Li
 * @date 2026-06-29 15:50:16
 */
public interface IHolidaysFaqGroupService {

    /**
     * 查询FAQ分组
     *
     * @param id 主键
     * @return FAQ分组
     */
    HolidaysFaqGroupVo queryById(Long id);

    /**
     * 分页查询FAQ分组列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return FAQ分组分页列表
     */
    PageResult<HolidaysFaqGroupVo> queryPageList(HolidaysFaqGroupBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的FAQ分组列表
     *
     * @param bo 查询条件
     * @return FAQ分组列表
     */
    List<HolidaysFaqGroupVo> queryList(HolidaysFaqGroupBo bo);


    /**
     * 新增FAQ分组
     *
     * @param bo FAQ分组
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysFaqGroupBo bo);

    /**
     * 修改FAQ分组
     *
     * @param bo FAQ分组
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysFaqGroupBo bo);

    /**
     * 修改FAQ分组状态
     *
     * @param id 主键
     * @param status 状态值
     * @return 是否修改成功
     */
    Boolean updateStatus(Long id, String status);

    /**
     * 查询启用的FAQ分组列表
     *
     * @return FAQ分组列表
     */
    List<HolidaysFaqGroupVo> queryEnabledList(Integer module);


    /**
     * 校验并批量删除FAQ分组信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



