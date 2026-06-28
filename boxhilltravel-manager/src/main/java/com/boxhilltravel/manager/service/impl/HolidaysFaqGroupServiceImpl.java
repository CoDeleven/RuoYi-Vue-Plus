package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.boxhilltravel.core.constant.HolidaysFaqConstants;
import com.boxhilltravel.core.domain.HolidaysFaqGroup;
import com.boxhilltravel.core.domain.HolidaysFaqItem;
import com.boxhilltravel.core.domain.bo.HolidaysFaqGroupBo;
import com.boxhilltravel.core.domain.vo.HolidaysFaqGroupVo;
import com.boxhilltravel.core.mapper.HolidaysFaqGroupMapper;
import com.boxhilltravel.core.mapper.HolidaysFaqItemMapper;
import com.boxhilltravel.manager.service.IHolidaysFaqGroupService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
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
 * FAQ分组Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-29 15:50:16
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysFaqGroupServiceImpl implements IHolidaysFaqGroupService {

    private static final String STATUS_ENABLED = "1";

    private final HolidaysFaqGroupMapper holidaysFaqGroupMapper;
    private final HolidaysFaqItemMapper holidaysFaqItemMapper;

/**
     * 查询FAQ分组
     *
     * @param id 主键
     * @return FAQ分组
     */
    @Override
    public HolidaysFaqGroupVo queryById(Long id) {
        return holidaysFaqGroupMapper.selectVoById(id);
    }

    /**
     * 分页查询FAQ分组列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return FAQ分组分页列表
     */
    @Override
    public PageResult<HolidaysFaqGroupVo> queryPageList(HolidaysFaqGroupBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysFaqGroup> lqw = buildQueryWrapper(bo);
        Page<HolidaysFaqGroupVo> result = holidaysFaqGroupMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的FAQ分组列表
     *
     * @param bo 查询条件
     * @return FAQ分组列表
     */
    @Override
    public List<HolidaysFaqGroupVo> queryList(HolidaysFaqGroupBo bo) {
        LambdaQueryWrapper<HolidaysFaqGroup> lqw = buildQueryWrapper(bo);
        return holidaysFaqGroupMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<HolidaysFaqGroup> buildQueryWrapper(HolidaysFaqGroupBo bo) {
        return QueryBuilder.lambda(HolidaysFaqGroup.class)
            .likeIfText(HolidaysFaqGroup::getGroupName, bo.getGroupName())
            .eqIfPresent(HolidaysFaqGroup::getModule, bo.getModule())
            .eqIfText(HolidaysFaqGroup::getStatus, bo.getStatus())
            .orderByAsc(HolidaysFaqGroup::getSortOrder)
            .orderByAsc(HolidaysFaqGroup::getId)
            .build();
    }

    /**
     * 新增FAQ分组
     *
     * @param bo FAQ分组
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysFaqGroupBo bo) {
        HolidaysFaqGroup add = MapstructUtils.convert(bo, HolidaysFaqGroup.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysFaqGroupMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改FAQ分组
     *
     * @param bo FAQ分组
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysFaqGroupBo bo) {
        HolidaysFaqGroup update = MapstructUtils.convert(bo, HolidaysFaqGroup.class);
        validEntityBeforeSave(update);
        return holidaysFaqGroupMapper.updateById(update) > 0;
    }

    /**
     * 修改FAQ分组状态
     *
     * @param id 主键
     * @param status 状态值
     * @return 是否修改成功
     */
    @Override
    public Boolean updateStatus(Long id, String status) {
        return holidaysFaqGroupMapper.lambda()
            .set(HolidaysFaqGroup::getStatus, status)
            .eq(HolidaysFaqGroup::getId, id)
            .update();
    }

    /**
     * 查询启用的FAQ分组列表
     *
     * @return FAQ分组列表
     */
    @Override
    public List<HolidaysFaqGroupVo> queryEnabledList(Integer module) {
        return holidaysFaqGroupMapper.selectVoList(QueryBuilder.lambda(HolidaysFaqGroup.class)
            .eq(HolidaysFaqGroup::getModule, ObjectUtil.defaultIfNull(module, HolidaysFaqConstants.DEFAULT_MODULE))
            .eq(HolidaysFaqGroup::getStatus, STATUS_ENABLED)
            .orderByAsc(HolidaysFaqGroup::getSortOrder)
            .orderByAsc(HolidaysFaqGroup::getId)
            .build());
    }


    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysFaqGroup entity) {
        entity.setModule(ObjectUtil.defaultIfNull(entity.getModule(), HolidaysFaqConstants.DEFAULT_MODULE));
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除FAQ分组信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            Long itemCount = holidaysFaqItemMapper.lambda()
                .in(HolidaysFaqItem::getGroupId, ids)
                .count();
            if (itemCount > 0) {
                throw new ServiceException("FAQ分组下存在条目，不能删除");
            }
        }
        return holidaysFaqGroupMapper.deleteByIds(ids) > 0;
    }
}


