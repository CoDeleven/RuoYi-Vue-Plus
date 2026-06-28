package com.boxhilltravel.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysDestination;
import com.boxhilltravel.core.domain.HolidaysDestinationPageContent;
import com.boxhilltravel.core.domain.bo.HolidaysDestinationPageContentBo;
import com.boxhilltravel.core.domain.vo.DestinationPageCityVo;
import com.boxhilltravel.core.domain.vo.DestinationPageHighlightVo;
import com.boxhilltravel.core.domain.vo.DestinationPagePracticalInfoVo;
import com.boxhilltravel.core.domain.vo.HolidaysDestinationPageContentVo;
import com.boxhilltravel.core.mapper.HolidaysDestinationMapper;
import com.boxhilltravel.core.mapper.HolidaysDestinationPageContentMapper;
import com.boxhilltravel.manager.service.IHolidaysDestinationPageContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.system.domain.SysDictData;
import org.dromara.system.mapper.SysDictDataMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Destination country page content service implementation.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HolidaysDestinationPageContentServiceImpl implements IHolidaysDestinationPageContentService {

    private static final Long COUNTRY_LEVEL = 2L;
    private static final Long STATUS_DRAFT = 0L;
    private static final Long STATUS_PUBLISHED = 1L;
    private static final String CURRENCY_DICT_TYPE = "holidays_currency_unit";
    private static final String LANGUAGE_DICT_TYPE = "holidays_destination_language";
    private static final String TIME_ZONE_DICT_TYPE = "holidays_destination_time_zone";

    private final HolidaysDestinationPageContentMapper pageContentMapper;
    private final HolidaysDestinationMapper destinationMapper;
    private final SysDictDataMapper dictDataMapper;

    @Override
    public HolidaysDestinationPageContentVo queryById(Long id) {
        HolidaysDestinationPageContentBo bo = new HolidaysDestinationPageContentBo();
        bo.setId(id);
        Page<HolidaysDestinationPageContentVo> page = pageContentMapper.selectDestinationPageContentPage(new Page<>(1, 1), bo);
        return page.getRecords().stream()
            .findFirst()
            .map(this::hydrateVo)
            .orElseGet(() -> hydrateVo(pageContentMapper.selectVoById(id)));
    }

    @Override
    public PageResult<HolidaysDestinationPageContentVo> queryPageList(HolidaysDestinationPageContentBo bo, PageQuery pageQuery) {
        Page<HolidaysDestinationPageContentVo> result = pageContentMapper.selectDestinationPageContentPage(pageQuery.build(), bo);
        result.getRecords().forEach(this::hydrateVo);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<HolidaysDestinationPageContentVo> queryList(HolidaysDestinationPageContentBo bo) {
        LambdaQueryWrapper<HolidaysDestinationPageContent> lqw = buildQueryWrapper(bo);
        List<HolidaysDestinationPageContentVo> list = pageContentMapper.selectVoList(lqw);
        list.forEach(this::hydrateVo);
        return list;
    }

    private LambdaQueryWrapper<HolidaysDestinationPageContent> buildQueryWrapper(HolidaysDestinationPageContentBo bo) {
        return QueryBuilder.lambda(HolidaysDestinationPageContent.class)
            .eqIfPresent(HolidaysDestinationPageContent::getDestinationId, bo.getDestinationId())
            .eqIfPresent(HolidaysDestinationPageContent::getStatus, bo.getStatus())
            .orderByDesc(HolidaysDestinationPageContent::getUpdateTime)
            .orderByDesc(HolidaysDestinationPageContent::getId)
            .build();
    }

    @Override
    public Boolean insertByBo(HolidaysDestinationPageContentBo bo) {
        HolidaysDestinationPageContent add = buildEntity(bo);
        validEntityBeforeSave(add, bo);
        boolean flag = pageContentMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(HolidaysDestinationPageContentBo bo) {
        HolidaysDestinationPageContent update = buildEntity(bo);
        validEntityBeforeSave(update, bo);
        return pageContentMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateStatus(Long id, Long status) {
        HolidaysDestinationPageContent entity = pageContentMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("Destination page content does not exist");
        }
        Long normalizedStatus = STATUS_PUBLISHED.equals(status) ? STATUS_PUBLISHED : STATUS_DRAFT;
        if (STATUS_PUBLISHED.equals(normalizedStatus)) {
            HolidaysDestinationPageContentVo vo = hydrateVo(pageContentMapper.selectVoById(id));
            validatePublish(vo);
        }
        return pageContentMapper.lambdaUpdate()
            .set(HolidaysDestinationPageContent::getStatus, normalizedStatus)
            .eq(HolidaysDestinationPageContent::getId, id)
            .update();
    }

    private HolidaysDestinationPageContent buildEntity(HolidaysDestinationPageContentBo bo) {
        HolidaysDestinationPageContent entity = new HolidaysDestinationPageContent();
        entity.setId(bo.getId());
        entity.setDestinationId(bo.getDestinationId());
        entity.setCurrencyDictValue(StringUtils.trim(bo.getCurrencyDictValue()));
        entity.setLanguageDictValue(StringUtils.trim(bo.getLanguageDictValue()));
        entity.setTimeZoneDictValue(StringUtils.trim(bo.getTimeZoneDictValue()));
        entity.setVisa(StringUtils.trim(bo.getVisa()));
        entity.setVisaTitle(StringUtils.trim(bo.getVisaTitle()));
        entity.setVisaNote(StringUtils.trim(bo.getVisaNote()));
        entity.setIntroductionJson(JsonUtils.toJsonString(normalizeStrings(bo.getIntroduction())));
        entity.setHighlightsJson(JsonUtils.toJsonString(normalizeHighlights(bo.getHighlights())));
        entity.setCitiesJson(JsonUtils.toJsonString(normalizeCities(bo.getCities())));
        entity.setPracticalInfoJson(JsonUtils.toJsonString(normalizePracticalInfo(bo.getPracticalInfo())));
        entity.setStatus(STATUS_PUBLISHED.equals(bo.getStatus()) ? STATUS_PUBLISHED : STATUS_DRAFT);
        return entity;
    }

    private void validEntityBeforeSave(HolidaysDestinationPageContent entity, HolidaysDestinationPageContentBo bo) {
        HolidaysDestination destination = destinationMapper.selectById(entity.getDestinationId());
        if (destination == null || destination.getDeletedAt() != null || !COUNTRY_LEVEL.equals(destination.getLevel())) {
            throw new ServiceException("Only existing country destinations can be bound");
        }
        validateDictValue(entity.getCurrencyDictValue(), CURRENCY_DICT_TYPE, false, "Currency");
        validateDictValue(entity.getLanguageDictValue(), LANGUAGE_DICT_TYPE, false, "Language");
        validateDictValue(entity.getTimeZoneDictValue(), TIME_ZONE_DICT_TYPE, false, "Time zone");
        Long existsCount = pageContentMapper.selectCount(QueryBuilder.lambda(HolidaysDestinationPageContent.class)
            .eq(HolidaysDestinationPageContent::getDestinationId, entity.getDestinationId())
            .ne(entity.getId() != null, HolidaysDestinationPageContent::getId, entity.getId())
            .build());
        if (existsCount != null && existsCount > 0) {
            throw new ServiceException("Destination page content already exists for this country");
        }
        if (STATUS_PUBLISHED.equals(entity.getStatus())) {
            HolidaysDestinationPageContentVo vo = hydrateVo(entityToVo(entity));
            validatePublish(vo);
        }
    }

    private void validatePublish(HolidaysDestinationPageContentVo vo) {
        List<String> missing = collectMissingItems(vo);
        if (!missing.isEmpty()) {
            throw new ServiceException("Publish failed. Missing: " + String.join(", ", missing));
        }
        validateDictValue(vo.getCurrencyDictValue(), CURRENCY_DICT_TYPE, true, "Currency");
        validateDictValue(vo.getLanguageDictValue(), LANGUAGE_DICT_TYPE, true, "Language");
        validateDictValue(vo.getTimeZoneDictValue(), TIME_ZONE_DICT_TYPE, true, "Time zone");
    }

    private void validateDictValue(String dictValue, String dictType, boolean required, String fieldName) {
        if (StringUtils.isBlank(dictValue)) {
            if (required) {
                throw new ServiceException(fieldName + " is required");
            }
            return;
        }
        Long count = dictDataMapper.selectCount(Wrappers.lambdaQuery(SysDictData.class)
            .eq(SysDictData::getDictType, dictType)
            .eq(SysDictData::getDictValue, dictValue));
        if (count == null || count == 0) {
            throw new ServiceException(fieldName + " dictionary value is invalid");
        }
    }

    private HolidaysDestinationPageContentVo hydrateVo(HolidaysDestinationPageContentVo vo) {
        if (vo == null) {
            return null;
        }
        vo.setIntroduction(parseArray(vo.getIntroductionJson(), String.class));
        vo.setHighlights(parseArray(vo.getHighlightsJson(), DestinationPageHighlightVo.class));
        vo.setCities(parseArray(vo.getCitiesJson(), DestinationPageCityVo.class));
        vo.setPracticalInfo(parseArray(vo.getPracticalInfoJson(), DestinationPagePracticalInfoVo.class));
        List<String> missingItems = collectMissingItems(vo);
        vo.setMissingItems(missingItems);
        vo.setCompleteness(calculateCompleteness(missingItems));
        return vo;
    }

    private HolidaysDestinationPageContentVo entityToVo(HolidaysDestinationPageContent entity) {
        HolidaysDestinationPageContentVo vo = new HolidaysDestinationPageContentVo();
        vo.setId(entity.getId());
        vo.setDestinationId(entity.getDestinationId());
        vo.setCurrencyDictValue(entity.getCurrencyDictValue());
        vo.setLanguageDictValue(entity.getLanguageDictValue());
        vo.setTimeZoneDictValue(entity.getTimeZoneDictValue());
        vo.setVisa(entity.getVisa());
        vo.setVisaTitle(entity.getVisaTitle());
        vo.setVisaNote(entity.getVisaNote());
        vo.setIntroductionJson(entity.getIntroductionJson());
        vo.setHighlightsJson(entity.getHighlightsJson());
        vo.setCitiesJson(entity.getCitiesJson());
        vo.setPracticalInfoJson(entity.getPracticalInfoJson());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    private List<String> collectMissingItems(HolidaysDestinationPageContentVo vo) {
        List<String> missing = new ArrayList<>();
        if (StringUtils.isBlank(vo.getCurrencyDictValue())) {
            missing.add("currency");
        }
        if (StringUtils.isBlank(vo.getLanguageDictValue())) {
            missing.add("language");
        }
        if (StringUtils.isBlank(vo.getTimeZoneDictValue())) {
            missing.add("time zone");
        }
        if (StringUtils.isBlank(vo.getVisa())) {
            missing.add("visa");
        }
        if (StringUtils.isBlank(vo.getVisaTitle())) {
            missing.add("visa title");
        }
        if (StringUtils.isBlank(vo.getVisaNote())) {
            missing.add("visa note");
        }
        if (vo.getIntroduction() == null || vo.getIntroduction().stream().noneMatch(StringUtils::isNotBlank)) {
            missing.add("introduction");
        }
        if (countValidHighlights(vo.getHighlights()) < 3) {
            missing.add("at least 3 highlights");
        }
        if (countValidCities(vo.getCities()) < 1) {
            missing.add("at least 1 city or attraction");
        }
        if (countValidPracticalInfo(vo.getPracticalInfo()) < 2) {
            missing.add("at least 2 practical info items");
        }
        return missing;
    }

    private int calculateCompleteness(List<String> missingItems) {
        int totalChecks = 10;
        int completed = Math.max(0, totalChecks - missingItems.size());
        return completed * 100 / totalChecks;
    }

    private int countValidHighlights(List<DestinationPageHighlightVo> list) {
        if (list == null) {
            return 0;
        }
        return (int) list.stream()
            .filter(item -> item != null && StringUtils.isNotBlank(item.getTitle()) && StringUtils.isNotBlank(item.getDescription()))
            .count();
    }

    private int countValidCities(List<DestinationPageCityVo> list) {
        if (list == null) {
            return 0;
        }
        return (int) list.stream()
            .filter(item -> item != null && StringUtils.isNotBlank(item.getName()) && StringUtils.isNotBlank(item.getDescription()))
            .count();
    }

    private int countValidPracticalInfo(List<DestinationPagePracticalInfoVo> list) {
        if (list == null) {
            return 0;
        }
        return (int) list.stream()
            .filter(item -> item != null && StringUtils.isNotBlank(item.getTitle()) && StringUtils.isNotBlank(item.getContent()))
            .count();
    }

    private List<String> normalizeStrings(List<String> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .toList();
    }

    private List<DestinationPageHighlightVo> normalizeHighlights(List<DestinationPageHighlightVo> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
            .filter(item -> item != null && (StringUtils.isNotBlank(item.getIcon()) || StringUtils.isNotBlank(item.getTitle()) || StringUtils.isNotBlank(item.getDescription())))
            .peek(item -> {
                item.setIcon(StringUtils.trim(item.getIcon()));
                item.setTitle(StringUtils.trim(item.getTitle()));
                item.setDescription(StringUtils.trim(item.getDescription()));
            })
            .toList();
    }

    private List<DestinationPageCityVo> normalizeCities(List<DestinationPageCityVo> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
            .filter(item -> item != null && (StringUtils.isNotBlank(item.getLabel()) || StringUtils.isNotBlank(item.getName())
                || StringUtils.isNotBlank(item.getImage()) || StringUtils.isNotBlank(item.getDescription())))
            .peek(item -> {
                item.setLabel(StringUtils.trim(item.getLabel()));
                item.setName(StringUtils.trim(item.getName()));
                item.setImage(StringUtils.trim(item.getImage()));
                item.setDescription(StringUtils.trim(item.getDescription()));
            })
            .toList();
    }

    private List<DestinationPagePracticalInfoVo> normalizePracticalInfo(List<DestinationPagePracticalInfoVo> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
            .filter(item -> item != null && (StringUtils.isNotBlank(item.getTitle()) || StringUtils.isNotBlank(item.getContent())))
            .peek(item -> {
                item.setTitle(StringUtils.trim(item.getTitle()));
                item.setContent(StringUtils.trim(item.getContent()));
            })
            .toList();
    }

    private <T> List<T> parseArray(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return List.of();
        }
        return JsonUtils.parseArray(json, clazz);
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return pageContentMapper.deleteByIds(ids) > 0;
    }

}
