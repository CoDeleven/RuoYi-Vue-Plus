package com.boxhilltravel.manager.domain.vo;

import com.boxhilltravel.core.domain.vo.CustomerContactInfoVo;
import com.boxhilltravel.core.domain.vo.CustomerProfileVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Customer user manager detail view object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerUserDetailVo extends CustomerUserVo {

    @Serial
    private static final long serialVersionUID = 1L;

    private String gender;

    private Long deptId;

    private String remark;

    private CustomerProfileVo profile;

    private CustomerContactInfoVo contactInfo;

}
