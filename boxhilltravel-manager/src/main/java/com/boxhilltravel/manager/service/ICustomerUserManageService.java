package com.boxhilltravel.manager.service;

import com.boxhilltravel.manager.domain.bo.CustomerUserBo;
import com.boxhilltravel.manager.domain.vo.CustomerUserDetailVo;
import com.boxhilltravel.manager.domain.vo.CustomerUserVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

/**
 * Customer user manager service.
 */
public interface ICustomerUserManageService {

    /**
     * Query customer users by page.
     *
     * @param bo query object
     * @param pageQuery page query
     * @return paged customer users
     */
    PageResult<CustomerUserVo> queryPageList(CustomerUserBo bo, PageQuery pageQuery);

    /**
     * Query customer user detail.
     *
     * @param userId customer user id
     * @return customer user detail
     */
    CustomerUserDetailVo queryById(Long userId);

    /**
     * Update customer user account status.
     *
     * @param userId customer user id
     * @param status account status
     * @return true when updated
     */
    Boolean updateStatus(Long userId, String status);

}
