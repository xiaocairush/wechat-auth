package com.example.auth.security;

import com.example.auth.security.oauth2.wechat.WechatMapOAuth2AccessTokenResponseConverter;
import com.example.auth.security.oauth2.wechat.WechatOAuth2AuthRequestBuilderCustomizer;
import com.example.auth.security.oauth2.wechat.WechatOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.example.auth.security.oauth2.wechat.WechatOAuth2UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.DelegatingOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * 参考：https://www.cnblogs.com/felordcn/p/15143384.html
 *
 * 针对微信网页登陆的OAuth2配置
 * <p>
 * Created by berg on 2023/4/7.
 */
@EnableWebSecurity(debug = true)
public class SecurityConfiguration {


    /**
     * 配置{@link HttpSecurity}的新方式
     *
     * @param httpSecurity                 the http security
     * @param clientRegistrationRepository the client registration repository
     * @return the security filter chain
     * @throws Exception the exception
     */
    @Bean
    @ConditionalOnMissingBean(
            name = {"defaultWebSecurityFilterChain"}
    )
    SecurityFilterChain defaultWebSecurityFilterChain(HttpSecurity httpSecurity,
                                                      ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        OAuth2AuthorizationRequestResolver authorizationRequestResolver = oAuth2AuthorizationRequestResolver(clientRegistrationRepository);

        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient = accessTokenResponseClient();

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DelegatingOAuth2UserService<>(Arrays.asList(new WechatOAuth2UserService(),
                new DefaultOAuth2UserService()));

        httpSecurity.authorizeRequests()
                .antMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .anyRequest().authenticated()
                .and().exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
//                .and().csrf().disable();
        // 如果需要拿授权方的用户信息需要走 oauth2login
        httpSecurity.oauth2Login()
                .successHandler(customAuthenticationSuccessHandler())
//                .defaultSuccessUrl("/oauth2/wx/h5/redirect")
                // 授权端点配置  比如获取 code
                .authorizationEndpoint().authorizationRequestResolver(authorizationRequestResolver)
                .and()
                // 获取token端点配置  比如根据code 获取 token
                .tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient)
                .and()
                // 获取用户信息端点配置  根据accessToken获取用户基本信息
                .userInfoEndpoint().userService(oAuth2UserService);

        // 如果不需要获取用户信息  仅仅是授权 就用 oauth2 client
//        httpSecurity.oauth2Client()
//                .authorizationCodeGrant().authorizationRequestResolver(authorizationRequestResolver)
//                .accessTokenResponseClient(accessTokenResponseClient);
        return httpSecurity.build();
    }

    private AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        // 使用前端请求中的参数作为重定向地址， 相关代码在WechatOAuth2AuthRequestBuilderCustomizer
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("state");
        return successHandler;
    }

    /**
     * 用来从{@link javax.servlet.http.HttpServletRequest}中检索Oauth2需要的参数并封装成OAuth2请求对象{@link OAuth2AuthorizationRequest}
     *
     * @param clientRegistrationRepository the client registration repository
     * @return DefaultOAuth2AuthorizationRequestResolver
     */
    private OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        resolver.setAuthorizationRequestCustomizer(WechatOAuth2AuthRequestBuilderCustomizer::customize);
        return resolver;
    }

    /**
     * 调用token-uri去请求授权服务器获取token的OAuth2 Http 客户端
     *
     * @return OAuth2AccessTokenResponseClient
     */
    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient tokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        tokenResponseClient.setRequestEntityConverter(new WechatOAuth2AuthorizationCodeGrantRequestEntityConverter());

        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
        // 微信返回的content-type 是 text-plain
        tokenResponseHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN,
                new MediaType("application", "*+json")));
        // 兼容微信解析
        tokenResponseHttpMessageConverter.setTokenResponseConverter(new WechatMapOAuth2AccessTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(
                Arrays.asList(new FormHttpMessageConverter(),
                        tokenResponseHttpMessageConverter
                ));

        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        tokenResponseClient.setRestOperations(restTemplate);
        return tokenResponseClient;
    }



}
