package com.boxhilltravel.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.HolidaysDestinationTagRel;
import com.boxhilltravel.core.domain.bo.HolidaysDestinationTagRelBo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationTagRelVo;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.core.mapper.HolidaysDestinationTagRelMapper;
import com.boxhilltravel.manager.service.IHolidaysDestinationTagRelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.system.domain.SysDictData;
import org.dromara.system.mapper.SysDictDataMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Destination Tag Service business implementation.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysDestinationTagRelServiceImpl implements IHolidaysDestinationTagRelService {

    private static final String DESTINATION_TAG_DICT_TYPE = "holidays_destination_tag";

    private final HolidaysDestinationTagRelMapper destinationTagRelMapper;
    private final HolidaysDestinationMapper destinationMapper;
    private final SysDictDataMapper dictDataMapper;

    @Override
    public HolidaysDestinationTagRelVo queryById(Long id) {
        HolidaysDestinationTagRelBo bo = new HolidaysDestinationTagRelBo();
        bo.setId(id);
        Page<HolidaysDestinationTagRelVo> page = destinationTagRelMapper.selectDestinationTagPage(new Page<>(1, 1), bo);
        return page.getRecords().stream().findFirst().orElse(destinationTagRelMapper.selectVoById(id));
    }

    @Override
    public PageResult<HolidaysDestinationTagRelVo> queryPageList(HolidaysDestinationTagRelBo bo, PageQuery pageQuery) {
        Page<HolidaysDestinationTagRelVo> result = destinationTagRelMapper.selectDestinationTagPage(pageQuery.build(), bo);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<HolidaysDestinationTagRelVo> queryList(HolidaysDestinationTagRelBo bo) {
        LambdaQueryWrapper<HolidaysDestinationTagRel> lqw = buildQueryWrapper(bo);
        return destinationTagRelMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<HolidaysDestinationTagRel> buildQueryWrapper(HolidaysDestinationTagRelBo bo) {
        return QueryBuilder.lambda(HolidaysDestinationTagRel.class)
            .eqIfPresent(HolidaysDestinationTagRel::getDestinationId, bo.getDestinationId())
            .eqIfText(HolidaysDestinationTagRel::getDictValue, bo.getDictValue())
            .orderByAsc(HolidaysDestinationTagRel::getSortOrder)
            .orderByAsc(HolidaysDestinationTagRel::getId)
            .build();
    }

    @Override
    public Boolean insertByBo(HolidaysDestinationTagRelBo bo) {
        HolidaysDestinationTagRel add = MapstructUtils.convert(bo, HolidaysDestinationTagRel.class);
        validEntityBeforeSave(add);
        boolean flag = destinationTagRelMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(HolidaysDestinationTagRelBo bo) {
        HolidaysDestinationTagRel update = MapstructUtils.convert(bo, HolidaysDestinationTagRel.class);
        validEntityBeforeSave(update);
        return destinationTagRelMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateSort(Long id, Integer sortValue) {
        return destinationTagRelMapper.lambda()
            .set(HolidaysDestinationTagRel::getSortOrder, sortValue)
            .eq(HolidaysDestinationTagRel::getId, id)
            .update();
    }

    private void validEntityBeforeSave(HolidaysDestinationTagRel entity) {
        HolidaysDestination destination = destinationMapper.selectById(entity.getDestinationId());
        if (destination == null || destination.getDeletedAt() != null) {
            throw new ServiceException(MessageUtils.message("boxhilltravel.error.destination.notFound"));
        }
        Long dictCount = dictDataMapper.lambda()
            .eq(SysDictData::getDictValue, entity.getDictValue())
            .eq(SysDictData::getDictType, DESTINATION_TAG_DICT_TYPE)
            .count();
        if (dictCount == null || dictCount == 0) {
            throw new ServiceException(MessageUtils.message("boxhilltravel.error.destinationTag.notFound"));
        }
        Long existsCount = destinationTagRelMapper.selectCount(QueryBuilder.lambda(HolidaysDestinationTagRel.class)
            .eq(HolidaysDestinationTagRel::getDestinationId, entity.getDestinationId())
            .eq(HolidaysDestinationTagRel::getDictValue, entity.getDictValue())
            .ne(entity.getId() != null, HolidaysDestinationTagRel::getId, entity.getId())
            .build());
        if (existsCount != null && existsCount > 0) {
            throw new ServiceException(MessageUtils.message("boxhilltravel.error.destinationTag.duplicate"));
        }
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return destinationTagRelMapper.deleteByIds(ids) > 0;
    }
}
