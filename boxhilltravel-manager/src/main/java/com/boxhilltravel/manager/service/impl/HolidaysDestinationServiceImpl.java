package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.bo.HolidaysDestinationBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationVo;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.manager.service.IHolidaysDestinationService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 目的地分类Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-27 16:41:24
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysDestinationServiceImpl implements IHolidaysDestinationService {

    private final HolidaysDestinationMapper holidaysDestinationMapper;

/**
     * 查询目的地分类
     *
     * @param id 主键
     * @return 目的地分类
     */
    @Override
    public HolidaysDestinationVo queryById(Long id) {
        return holidaysDestinationMapper.selectVoById(id);
    }

    /**
     * 分页查询目的地分类列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 目的地分类分页列表
     */
    @Override
    public PageResult<HolidaysDestinationVo> queryPageList(HolidaysDestinationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysDestination> lqw = buildQueryWrapper(bo);
        Page<HolidaysDestinationVo> result = holidaysDestinationMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的目的地分类列表
     *
     * @param bo 查询条件
     * @return 目的地分类列表
     */
    @Override
    public List<HolidaysDestinationVo> queryList(HolidaysDestinationBo bo) {
        LambdaQueryWrapper<HolidaysDestination> lqw = buildQueryWrapper(bo);
        return holidaysDestinationMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<HolidaysDestination> buildQueryWrapper(HolidaysDestinationBo bo) {
        Map<String, Object> params = bo.getParams();
        return QueryBuilder.lambda(HolidaysDestination.class)
            .likeIfText(HolidaysDestination::getName, bo.getName())
            .eqIfText(HolidaysDestination::getNameEn, bo.getNameEn())
            .eqIfPresent(HolidaysDestination::getParentId, bo.getParentId())
            .eqIfPresent(HolidaysDestination::getLevel, bo.getLevel())
            .eqIfText(HolidaysDestination::getImage, bo.getImage())
            .eqIfText(HolidaysDestination::getDescription, bo.getDescription())
            .eqIfPresent(HolidaysDestination::getSort, bo.getSort())
            .eqIfPresent(HolidaysDestination::getStatus, bo.getStatus())
            .betweenParams(HolidaysDestination::getDeletedAt, params, "beginDeletedAt", "endDeletedAt")
            .orderByAsc(HolidaysDestination::getId)
            .build();
    }

    /**
     * 新增目的地分类
     *
     * @param bo 目的地分类
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysDestinationBo bo) {
        HolidaysDestination add = MapstructUtils.convert(bo, HolidaysDestination.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysDestinationMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改目的地分类
     *
     * @param bo 目的地分类
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysDestinationBo bo) {
        HolidaysDestination update = MapstructUtils.convert(bo, HolidaysDestination.class);
        validEntityBeforeSave(update);
        return holidaysDestinationMapper.updateById(update) > 0;
    }



    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysDestination entity) {
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除目的地分类信息
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
        return holidaysDestinationMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<HolidaysDestinationVo> queryByIds(String ids) {
        List<String> split = StrUtil.split(ids, ",");
        return holidaysDestinationMapper.selectVoByIds(split);
    }
}


