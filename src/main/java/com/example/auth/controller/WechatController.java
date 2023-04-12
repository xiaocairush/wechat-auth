package com.example.auth.controller;


import com.example.auth.security.oauth2.wechat.WechatOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Wechat controller.
 *
 * @author n1
 * @since 2021 /8/12 16:45
 */
@Slf4j
@RestController
@RequestMapping("/wx/")
public class WechatController {

    @RequestMapping("/h5/userinfo")
    @ResponseBody
    public WechatOAuth2User userInfo(@AuthenticationPrincipal WechatOAuth2User principal) {
        return principal;
    }

}
