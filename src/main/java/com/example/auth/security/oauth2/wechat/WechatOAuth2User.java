package com.example.auth.security.oauth2.wechat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 微信授权的OAuth2User用户信息
 *
 * @author n1
 * @since 2021/8/12 17:37
 */
@NoArgsConstructor
@Data
public class WechatOAuth2User implements OAuth2User, Serializable {
    private String openid;
    private String nickname;
    private Integer sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private List<String> privilege;
    private String unionid;


    @JsonIgnore
    @Override
    public Map<String, Object> getAttributes() {
        // 原本返回前端token 但是微信给的token比较敏感 所以不返回
        return Collections.emptyMap();
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 这里放scopes 或者其它你业务逻辑相关的用户权限集 目前没有什么用
        return null;
    }

    @JsonIgnore
    @Override
    public String getName() {
        // 用户唯一标识比较合适，这个不能为空啊，如果你能保证unionid不为空，也是不错的选择。
        return openid;
    }
}
