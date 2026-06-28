package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysFaqItemBo;
import com.boxhilltravel.core.domain.vo.HolidaysFaqItemVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * FAQ条目Service接口
 *
 * @author Lion Li
 * @date 2026-06-29 15:46:06
 */
public interface IHolidaysFaqItemService {

    /**
     * 查询FAQ条目
     *
     * @param id 主键
     * @return FAQ条目
     */
    HolidaysFaqItemVo queryById(Long id);

    /**
     * 分页查询FAQ条目列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return FAQ条目分页列表
     */
    PageResult<HolidaysFaqItemVo> queryPageList(HolidaysFaqItemBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的FAQ条目列表
     *
     * @param bo 查询条件
     * @return FAQ条目列表
     */
    List<HolidaysFaqItemVo> queryList(HolidaysFaqItemBo bo);


    /**
     * 新增FAQ条目
     *
     * @param bo FAQ条目
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysFaqItemBo bo);

    /**
     * 修改FAQ条目
     *
     * @param bo FAQ条目
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysFaqItemBo bo);

    /**
     * 修改FAQ条目状态
     *
     * @param id 主键
     * @param status 状态值
     * @return 是否修改成功
     */
    Boolean updateStatus(Long id, String status);


    /**
     * 校验并批量删除FAQ条目信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



