package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.HolidaysHomeBannerBo;
import com.boxhilltravel.core.domain.vo.HolidaysHomeBannerVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 首页横幅Service接口
 *
 * @author BoxHillTravel
 * @date 2026-09-19
 */
public interface IHolidaysHomeBannerService {

    /**
     * 查询首页横幅
     *
     * @param id 主键
     * @return 首页横幅
     */
    HolidaysHomeBannerVo queryById(Long id);

    /**
     * 分页查询首页横幅列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 首页横幅分页列表
     */
    PageResult<HolidaysHomeBannerVo> queryPageList(HolidaysHomeBannerBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的所有首页横幅列表
     *
     * @param bo 查询条件
     * @return 首页横幅列表
     */
    List<HolidaysHomeBannerVo> queryList(HolidaysHomeBannerBo bo);

    /**
     * 新增首页横幅
     *
     * @param bo 首页横幅
     * @return 是否新增成功
     */
    Boolean insertByBo(HolidaysHomeBannerBo bo);

    /**
     * 修改首页横幅
     *
     * @param bo 首页横幅
     * @return 是否修改成功
     */
    Boolean updateByBo(HolidaysHomeBannerBo bo);

    /**
     * 校验并批量删除首页横幅信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}