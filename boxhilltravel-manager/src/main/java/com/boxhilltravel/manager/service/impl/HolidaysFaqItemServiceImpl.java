package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.boxhilltravel.core.constant.HolidaysFaqConstants;
import com.boxhilltravel.core.domain.HolidaysFaqItem;
import com.boxhilltravel.core.domain.bo.HolidaysFaqItemBo;
import com.boxhilltravel.core.domain.vo.HolidaysFaqItemVo;
import com.boxhilltravel.core.mapper.HolidaysFaqItemMapper;
import com.boxhilltravel.manager.service.IHolidaysFaqItemService;
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
 * FAQ条目Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-29 15:46:06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysFaqItemServiceImpl implements IHolidaysFaqItemService {

    private final HolidaysFaqItemMapper holidaysFaqItemMapper;

/**
     * 查询FAQ条目
     *
     * @param id 主键
     * @return FAQ条目
     */
    @Override
    public HolidaysFaqItemVo queryById(Long id) {
        return holidaysFaqItemMapper.selectFaqItemVoById(id);
    }

    /**
     * 分页查询FAQ条目列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return FAQ条目分页列表
     */
    @Override
    public PageResult<HolidaysFaqItemVo> queryPageList(HolidaysFaqItemBo bo, PageQuery pageQuery) {
        Page<HolidaysFaqItemVo> result = holidaysFaqItemMapper.selectFaqItemVoPage(pageQuery.build(), bo);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的FAQ条目列表
     *
     * @param bo 查询条件
     * @return FAQ条目列表
     */
    @Override
    public List<HolidaysFaqItemVo> queryList(HolidaysFaqItemBo bo) {
        return holidaysFaqItemMapper.selectFaqItemVoList(bo);
    }


    private LambdaQueryWrapper<HolidaysFaqItem> buildQueryWrapper(HolidaysFaqItemBo bo) {
        return QueryBuilder.lambda(HolidaysFaqItem.class)
            .eqIfPresent(HolidaysFaqItem::getGroupId, bo.getGroupId())
            .eqIfPresent(HolidaysFaqItem::getModule, bo.getModule())
            .likeIfText(HolidaysFaqItem::getQuestion, bo.getQuestion())
            .eqIfText(HolidaysFaqItem::getStatus, bo.getStatus())
            .orderByAsc(HolidaysFaqItem::getSortOrder)
            .orderByAsc(HolidaysFaqItem::getId)
            .build();
    }

    /**
     * 新增FAQ条目
     *
     * @param bo FAQ条目
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysFaqItemBo bo) {
        HolidaysFaqItem add = MapstructUtils.convert(bo, HolidaysFaqItem.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysFaqItemMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改FAQ条目
     *
     * @param bo FAQ条目
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysFaqItemBo bo) {
        HolidaysFaqItem update = MapstructUtils.convert(bo, HolidaysFaqItem.class);
        validEntityBeforeSave(update);
        return holidaysFaqItemMapper.updateById(update) > 0;
    }

    /**
     * 修改FAQ条目状态
     *
     * @param id 主键
     * @param status 状态值
     * @return 是否修改成功
     */
    @Override
    public Boolean updateStatus(Long id, String status) {
        return holidaysFaqItemMapper.lambda()
            .set(HolidaysFaqItem::getStatus, status)
            .eq(HolidaysFaqItem::getId, id)
            .update();
    }


    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysFaqItem entity) {
        entity.setModule(ObjectUtil.defaultIfNull(entity.getModule(), HolidaysFaqConstants.DEFAULT_MODULE));
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除FAQ条目信息
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
        return holidaysFaqItemMapper.deleteByIds(ids) > 0;
    }
}


