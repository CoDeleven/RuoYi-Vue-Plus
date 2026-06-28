package com.boxhilltravel.manager.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Customer user manager query object.
 */
@Data
public class CustomerUserBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String userName;

    private String nickName;

    private String email;

    private String phoneNumber;

    private String status;

    private Map<String, Object> params = new HashMap<>();

}
