package com.npu.gmall.ssoserver.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 单点登录
     * @param redirect_url
     * @param ssoUser
     * @param model
     * @return
     */

    @GetMapping("/login")
    public String login(@RequestParam("redirect_url") String redirect_url,
                        @CookieValue(value = "sso_user",required = false) String ssoUser,
                        Model model){
        //1、判断之前是否登录过
        if(!StringUtils.isEmpty(ssoUser)){
            //登录过,回到之前的地方，并且把ssoserver获取到的cookie以url方式传递给其它域名
            return "redirect:"+redirect_url+"?sso_user="+ssoUser;
        }else{
            //没有登录过
            model.addAttribute("redirect_url",redirect_url);
            return "login";
        }
    }

    @PostMapping("/doLogin")
    public String doLogin(String username, String password, HttpServletResponse response,String redirect_url){
        //模拟用户登录
        Map<String,Object> map=new HashMap<>();
        map.put("username",username);
        map.put("email",username+"@qq.com");

        //以上表示用户登录成功，将用户信息放入redis中
        String token = UUID.randomUUID().toString().replace("-","");

        stringRedisTemplate.opsForValue().set(token,JSON.toJSONString(map));

        //处理完后做两件事 1、命令浏览器把当前的token保存为cookie sso_user=token
        //                2、命令浏览器重定向到它之前的位置
        Cookie cookie=new Cookie("sso_user",token);
        response.addCookie(cookie);

        return "redirect:"+redirect_url+"?sso_user"+token;
    }

}
