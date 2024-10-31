package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RequestMapping("/user")
@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取用户输入的手机号码
        String phone = user.getPhone();
        if (StringUtils.isEmpty(phone)) {
            throw new CustomException("输入的邮箱地址为空!");
        }
        //获取四位验证码
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("code: {}", code);//偷看验证码
        //发送验证码
        SMSUtils.sendMessage(phone, code);
        //将发送成功的验证码存在session中
        session.setAttribute(phone, code);
        return R.success("发送成功!");
    }


    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();

        //获取用户输入的验证码
        String code = map.get("code").toString();

        //获取实际上的验证码
        String realCode = (String) session.getAttribute(phone);

        //比对验证码
        if (realCode == null || realCode.equals(code)) {
            //先判断是不是新用户 是的话就自动注册
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, phone);
            User user = userService.getOne(wrapper);
            if (user == null) {
                //是新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登录错误..");
    }


}
