package com.boxhilltravel.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Customer login token response.
 */
@Data
public class CustomerLoginVo {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expire_in")
    private Long expireIn;

    @JsonProperty("refresh_expire_in")
    private Long refreshExpireIn;

    @JsonProperty("client_id")
    private String clientId;

    private String scope;

    private String openid;

}
