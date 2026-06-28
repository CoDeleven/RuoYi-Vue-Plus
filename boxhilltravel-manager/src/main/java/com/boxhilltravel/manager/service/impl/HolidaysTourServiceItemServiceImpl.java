package com.boxhilltravel.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.boxhilltravel.core.domain.bo.HolidaysTourServiceItemBo;
import com.boxhilltravel.core.domain.vo.HolidaysTourServiceItemVo;
import com.boxhilltravel.core.mapper.HolidaysTourServiceItemMapper;
import com.boxhilltravel.manager.service.IHolidaysTourServiceItemService;
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
import com.boxhilltravel.core.domain.HolidaysTourServiceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 线路服务项Service业务层处理
 *
 * @author Lion Li
 * @date 2026-06-28 21:44:19
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysTourServiceItemServiceImpl implements IHolidaysTourServiceItemService {

    private final HolidaysTourServiceItemMapper holidaysTourServiceItemMapper;

/**
     * 查询线路服务项
     *
     * @param id 主键
     * @return 线路服务项
     */
    @Override
    public HolidaysTourServiceItemVo queryById(Long id) {
        return holidaysTourServiceItemMapper.selectVoById(id);
    }

    /**
     * 分页查询线路服务项列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 线路服务项分页列表
     */
    @Override
    public PageResult<HolidaysTourServiceItemVo> queryPageList(HolidaysTourServiceItemBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HolidaysTourServiceItem> lqw = buildQueryWrapper(bo);
        Page<HolidaysTourServiceItemVo> result = holidaysTourServiceItemMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    /**
     * 查询符合条件的线路服务项列表
     *
     * @param bo 查询条件
     * @return 线路服务项列表
     */
    @Override
    public List<HolidaysTourServiceItemVo> queryList(HolidaysTourServiceItemBo bo) {
        LambdaQueryWrapper<HolidaysTourServiceItem> lqw = buildQueryWrapper(bo);
        return holidaysTourServiceItemMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<HolidaysTourServiceItem> buildQueryWrapper(HolidaysTourServiceItemBo bo) {
        return QueryBuilder.lambda(HolidaysTourServiceItem.class)
            .eqIfPresent(HolidaysTourServiceItem::getTourId, bo.getTourId())
            .eqIfPresent(HolidaysTourServiceItem::getItemType, bo.getItemType())
            .eqIfText(HolidaysTourServiceItem::getContent, bo.getContent())
            .eqIfPresent(HolidaysTourServiceItem::getSortOrder, bo.getSortOrder())
            .orderByAsc(HolidaysTourServiceItem::getId)
            .build();
    }

    /**
     * 新增线路服务项
     *
     * @param bo 线路服务项
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(HolidaysTourServiceItemBo bo) {
        HolidaysTourServiceItem add = MapstructUtils.convert(bo, HolidaysTourServiceItem.class);
        validEntityBeforeSave(add);
        boolean flag = holidaysTourServiceItemMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改线路服务项
     *
     * @param bo 线路服务项
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(HolidaysTourServiceItemBo bo) {
        HolidaysTourServiceItem update = MapstructUtils.convert(bo, HolidaysTourServiceItem.class);
        validEntityBeforeSave(update);
        return holidaysTourServiceItemMapper.updateById(update) > 0;
    }



    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(HolidaysTourServiceItem entity) {
        // 可在此扩展通用业务校验
    }


    /**
     * 校验并批量删除线路服务项信息
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
        return holidaysTourServiceItemMapper.deleteByIds(ids) > 0;
    }
}


