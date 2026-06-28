package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysHotDealTourBo;
import com.boxhilltravel.core.domain.vo.HolidaysHotDealTourVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 热卖线路Service接口
 *
 * @author Lion Li
 * @date 2026-06-28 23:38:47
 */
public interface IHolidaysHotDealTourService {

    /**
     * 查询热卖线路
     *
     * @param id 主键
     * @return 热卖线路
     */
    HolidaysHotDealTourVo queryById(Long id);

    /**
     * 分页查询热卖线路列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 热卖线路分页列表
     */
    PageResult<HolidaysHotDealTourVo> queryPageList(HolidaysHotDealTourBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的热卖线路列表
     *
     * @param bo 查询条件
     * @return 热卖线路列表
     */
    List<HolidaysHotDealTourVo> queryList(HolidaysHotDealTourBo bo);


    /**
     * 新增热卖线路
     *
     * @param bo 热卖线路
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysHotDealTourBo bo);

    /**
     * 修改热卖线路
     *
     * @param bo 热卖线路
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysHotDealTourBo bo);



    /**
     * 校验并批量删除热卖线路信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}



