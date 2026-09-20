package com.boxhilltravel.manager.service.impl;

import com.boxhilltravel.core.domain.HolidaysHomeBanner;
import com.boxhilltravel.core.domain.bo.HolidaysHomeBannerBo;
import com.boxhilltravel.core.domain.vo.HolidaysHomeBannerVo;
import com.boxhilltravel.core.mapper.HolidaysHomeBannerMapper;
import com.boxhilltravel.manager.service.IHolidaysHomeBannerService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 首页横幅Service业务层处理
 *
 * @author BoxHillTravel
 * @date 2026-09-19
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysHomeBannerServiceImpl implements IHolidaysHomeBannerService {

    private final HolidaysHomeBannerMapper holidaysHomeBannerMapper;

    /**
     * 查询首页横幅
     *
     * @param id 主键
     * @return 首页横幅
     */
    @Override
    public HolidaysHomeBannerVo queryById(Long id) {
        return holidaysHomeBannerMapper.selectVoById(id);
    }

    /**
     * 分页查询首页横幅列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 首页横幅分页列表
     */
    @Override
    public PageResult<HolidaysHomeBannerVo> queryPageList(HolidaysHomeBannerBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysHomeBanner> lqw = buildQueryWrapper(bo);
        Page<HolidaysHomeBannerVo> result = holidaysHomeBannerMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的所有首页横幅列表
     *
     * @param bo 查询条件
     * @return 首页横幅列表
     */
    @Override
    public List<HolidaysHomeBannerVo> queryList(HolidaysHomeBannerBo bo) {
        LambdaQueryWrapper<HolidaysHomeBanner> lqw = buildQueryWrapper(bo);
        return holidaysHomeBannerMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<HolidaysHomeBanner> buildQueryWrapper(HolidaysHomeBannerBo bo) {
        return QueryBuilder.lambda(HolidaysHomeBanner.class)
            .eqIfPresent(HolidaysHomeBanner::getTitle, bo.getTitle())
            .eqIfPresent(HolidaysHomeBanner::getStatus, bo.getStatus())
            .orderByAsc(HolidaysHomeBanner::getSortOrder)
            .orderByAsc(HolidaysHomeBanner::getId)
            .build();
    }

    /**
     * 新增首页横幅
     *
     * @param bo 首页横幅
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysHomeBannerBo bo) {
        HolidaysHomeBanner add = MapstructUtils.convert(bo, HolidaysHomeBanner.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysHomeBannerMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改首页横幅
     *
     * @param bo 首页横幅
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysHomeBannerBo bo) {
        HolidaysHomeBanner update = MapstructUtils.convert(bo, HolidaysHomeBanner.class);
        validEntityBeforeSave(update);
        return holidaysHomeBannerMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysHomeBanner entity) {
        // 可在此扩展通用业务校验
    }

    /**
     * 校验并批量删除首页横幅信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 可在此扩展删除前业务校验
        }
        return holidaysHomeBannerMapper.deleteByIds(ids) > 0;
    }
}