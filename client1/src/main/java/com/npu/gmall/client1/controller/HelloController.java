package com.npu.gmall.client1.controller;

import com.npu.gmall.client1.config.SsoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HelloController {

    @Autowired
    SsoConfig ssoConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * sso服务器登录成了会在url后面给我们带一个cookie
     * @param model
     * @param ssoUserCookie
     * @param request
     * @return
     */
    @GetMapping("/")
    public String index(Model model,
                        @CookieValue(value = "sso_user",required =false) String ssoUserCookie,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam(value = "sso_user",required =false) String ssoUserParam){
        if(!StringUtils.isEmpty(ssoUserParam)){
            //登录服务器会在url上携带了要保存的cookie的信息，通知浏览器进行保存
            Cookie cookie = new Cookie("sso_user", ssoUserParam);
            response.addCookie(cookie);
            return "index";
        }
        StringBuffer requestURL = request.getRequestURL();

        //1、判断是否登录了
        if(StringUtils.isEmpty(ssoUserCookie)){
            //没登录,重定向到登陆服务器
            return "redirect:"+ssoConfig.getUrl()+ssoConfig.getLoginPath()+"?redirect_url="+requestURL.toString();
        }else{
            //登录了,redis.get(ssoUserCookie)获取到用户信息
            String userInfo = stringRedisTemplate.opsForValue().get(ssoUserCookie);
            model.addAttribute("loginUser",userInfo);
            return "index";
        }
    }

}
