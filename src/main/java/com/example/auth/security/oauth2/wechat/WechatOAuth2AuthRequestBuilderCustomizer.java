package com.example.auth.security.oauth2.wechat;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * Created by berg on 2023/4/7.
 */
public class WechatOAuth2AuthRequestBuilderCustomizer {
    private static final String WECHAT_ID = "wechat";

    /**
     * 使用构造器来构造获取授权码的uri
     *
     * @param builder 构造器
     */
    public static void customize(OAuth2AuthorizationRequest.Builder builder) {
        String regId = builder.build()
                .getAttributes()
                .get(OAuth2ParameterNames.REGISTRATION_ID)
                .toString();
        // 使用客户端参数中重定向地址作为授权之后重定向的地址
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        builder.state(request.getParameter("redirect_url"));

        if (WECHAT_ID.equals(regId)) {
            builder.authorizationRequestUri(WechatOAuth2RequestUriBuilderCustomizer::customize);
        }
    }

    /**
     * 定制微信网页登陆OAuth2请求URI
     *
     */
    private static class WechatOAuth2RequestUriBuilderCustomizer {

        /**
         * 默认情况下Spring Security会生成授权链接：
         * {@code https://open.weixin.qq.com/connect/qrconnect?response_type=code
         * &appid=wx82882b49d4424d99
         * &redirect_uri=REDIRECT_URI
         * &scope=snsapi_login
         * &state=STATE#wechat_redirect }
         *
         * 缺少了微信协议要求的{@code #wechat_redirect}，同时 {@code client_id}应该替换为{@code app_id}
         *
         * @param builder the builder
         * @return the uri
         */
        public static URI customize(UriBuilder builder) {
            String reqUri = builder.build().toString()
                    .replaceAll("client_id=", "appid=")
                    .concat("#wechat_redirect");
            return URI.create(reqUri);
        }
    }
}
