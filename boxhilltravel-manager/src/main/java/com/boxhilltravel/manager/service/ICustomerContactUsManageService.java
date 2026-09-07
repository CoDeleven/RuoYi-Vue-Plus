package com.boxhilltravel.manager.service;

import com.boxhilltravel.core.domain.bo.CustomerContactUsBo;
import com.boxhilltravel.core.domain.vo.CustomerContactUsVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;

/**
 * Customer contact us manage service.
 */
public interface ICustomerContactUsManageService {

    CustomerContactUsVo queryById(Long id);

    PageResult<CustomerContactUsVo> queryPageList(CustomerContactUsBo bo, PageQuery pageQuery);

    Boolean updateReadStatus(Long id, Integer readStatus);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
