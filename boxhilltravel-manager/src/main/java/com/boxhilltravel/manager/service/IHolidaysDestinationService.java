package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysDestinationBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 目的地分类Service接口
 *
 * @author Lion Li
 * @date 2026-06-27 16:41:24
 */
public interface IHolidaysDestinationService {

    /**
     * 查询目的地分类
     *
     * @param id 主键
     * @return 目的地分类
     */
    HolidaysDestinationVo queryById(Long id);

    /**
     * 分页查询目的地分类列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 目的地分类分页列表
     */
    PageResult<HolidaysDestinationVo> queryPageList(HolidaysDestinationBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的目的地分类列表
     *
     * @param bo 查询条件
     * @return 目的地分类列表
     */
    List<HolidaysDestinationVo> queryList(HolidaysDestinationBo bo);


    /**
     * 新增目的地分类
     *
     * @param bo 目的地分类
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysDestinationBo bo);

    /**
     * 修改目的地分类
     *
     * @param bo 目的地分类
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysDestinationBo bo);



    /**
     * 校验并批量删除目的地分类信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 批量查询ids
     * @param ids id列表
     * @return
     */
    List<HolidaysDestinationVo> queryByIds(String ids);
}



