# wechat_auth

## 介绍
微信登录没有遵从oath2.0标准，同时针对前后端分离的场景，spring security需要自己扩展开发一些组件，本项目使用spring security对微信登陆做了适配，同时记录session到redis, 如果有类似的开发需求可以参考本项目作为例子，节省开发时间。

## 用法介绍

1. application.yaml中修改client-id和client-secret
2. 针对未授权的情况在CustomAuthenticationEntryPoint中返回你想要的json串
3. 启动后浏览器访问：http://localhost:8080/oauth2/authorization/wechat?redirect_url=xxx
4. 前端访问 http://localhost:8080/wx/h5/userinfo 获取认证后的用户信息

## 说明

1. redirect_url是认证成功后返回的前端地址
2. 仅供作为样例参考，线上谨慎使用，初衷是为自己存档代码