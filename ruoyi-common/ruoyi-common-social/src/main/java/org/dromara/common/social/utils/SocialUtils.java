package org.dromara.common.social.utils;

import cn.hutool.core.util.ObjectUtil;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.social.config.properties.SocialLoginConfigProperties;
import org.dromara.common.social.config.properties.SocialProperties;

/**
 * Social authorization utility.
 *
 * @author thiszhc
 */
public class SocialUtils {

    private static final AuthRedisStateCache STATE_CACHE = SpringUtils.getBean(AuthRedisStateCache.class);

    public static AuthResponse<AuthUser> loginAuth(String source, String code, String state, SocialProperties socialProperties) throws AuthException {
        AuthRequest authRequest = getAuthRequest(source, socialProperties);
        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        return authRequest.login(callback);
    }

    public static AuthRequest getAuthRequest(String source, SocialProperties socialProperties) throws AuthException {
        SocialLoginConfigProperties obj = socialProperties.getType().get(source);
        if (ObjectUtil.isNull(obj)) {
            throw new AuthException("Unsupported social login source");
        }
        AuthConfig.AuthConfigBuilder builder = AuthConfig.builder()
            .clientId(obj.getClientId())
            .clientSecret(obj.getClientSecret())
            .redirectUri(obj.getRedirectUri())
            .serverUrl(obj.getServerUrl())
            .scopes(obj.getScopes());
        switch (source.toLowerCase()) {
            case "dingtalk":
                return new AuthDingTalkV2Request(builder.build(), STATE_CACHE);
            case "baidu":
                return new AuthBaiduRequest(builder.build(), STATE_CACHE);
            case "github":
                return new AuthGithubRequest(builder.build(), STATE_CACHE);
            case "gitee":
                return new AuthGiteeRequest(builder.build(), STATE_CACHE);
            case "weibo":
                return new AuthWeiboRequest(builder.build(), STATE_CACHE);
            case "coding":
                return new AuthCodingRequest(builder.build(), STATE_CACHE);
            case "oschina":
                return new AuthOschinaRequest(builder.build(), STATE_CACHE);
            case "alipay_wallet":
                return new AuthAlipayRequest(builder.build(), socialProperties.getType().get("alipay_wallet").getAlipayPublicKey(), STATE_CACHE);
            case "qq":
                return new AuthQqRequest(builder.build(), STATE_CACHE);
            case "wechat_open":
                return new AuthWeChatOpenRequest(builder.build(), STATE_CACHE);
            case "taobao":
                return new AuthTaobaoRequest(builder.build(), STATE_CACHE);
            case "douyin":
                return new AuthDouyinRequest(builder.build(), STATE_CACHE);
            case "linkedin":
                return new AuthLinkedinRequest(builder.build(), STATE_CACHE);
            case "microsoft":
                return new AuthMicrosoftRequest(builder.tenantId(obj.getTenantId()).build(), STATE_CACHE);
            case "renren":
                return new AuthRenrenRequest(builder.build(), STATE_CACHE);
            case "stack_overflow":
                return new AuthStackOverflowRequest(builder.stackOverflowKey(obj.getStackOverflowKey()).build(), STATE_CACHE);
            case "huawei":
                return new AuthHuaweiV3Request(builder.build(), STATE_CACHE);
            case "wechat_enterprise":
                return new AuthWeChatEnterpriseQrcodeV2Request(builder.agentId(obj.getAgentId()).build(), STATE_CACHE);
            case "gitlab":
                return new AuthGitlabRequest(builder.build(), STATE_CACHE);
            case "wechat_mp":
                return new AuthWeChatMpRequest(builder.build(), STATE_CACHE);
            case "aliyun":
                return new AuthAliyunRequest(builder.build(), STATE_CACHE);
            case "facebook":
                return new AuthFacebookRequest(builder.build(), STATE_CACHE);
            case "google":
                return new AuthGoogleRequest(builder.build(), STATE_CACHE);
            case "maxkey":
                return new AuthMaxKeyRequest(builder.build(), STATE_CACHE);
            case "topiam":
                return new AuthTopIamRequest(builder.build(), STATE_CACHE);
            case "gitea":
                return new AuthGiteaRequest(builder.build(), STATE_CACHE);
            default:
                throw new AuthException("Unsupported social login source");
        }
    }
}
