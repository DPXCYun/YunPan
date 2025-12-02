package com.example.yunpiyuanpan.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yunpiyuanpan.mapper.YPUserMapper;
import com.example.yunpiyuanpan.pojo.YPUser;
import com.example.yunpiyuanpan.service.LoginService;
import com.example.yunpiyuanpan.util.R;
import com.example.yunpiyuanpan.util.ResultCodeEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "用户登录接口")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private YPUserMapper ypUserMapper;

    @PostMapping("/login")
    @Operation(summary = "登录")
    public R login(@Param("account") String account, @Param("password") String password){//测试成功
        System.out.println(account + password);
        if(loginService.isLoginLegal(account, password)){//用户名密码满足条件
            //登录成功，返回用户表内容
            QueryWrapper wrapper1 = new QueryWrapper();
            wrapper1.eq("email",account);
            YPUser ypUser = ypUserMapper.selectOne(wrapper1);
            if(ypUser!=null){
                StpUtil.login(ypUser.getId());//创建登录凭证
                return R.ok().data("user", ypUser).data("satoken",StpUtil.getTokenValue());
            }else{
                QueryWrapper wrapper2 = new QueryWrapper();
                wrapper2.eq("phone",account);
                ypUser = ypUserMapper.selectOne(wrapper2);
                StpUtil.login(ypUser.getId());//创建登录凭证
                return R.ok().data("user", ypUser).data("satoken",StpUtil.getTokenValue());
            }
        }
        return R.setResult(ResultCodeEnum.FAIL).message("邮箱/手机号 或 密码 不正确");
    }

    @PostMapping("/forgetPassword")
    @Operation(summary = "忘记密码")
    public R forgotPassword(@Param("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            return R.fail().message("邮箱不能为空");
        }

        if (loginService.sendPasswordResetEmail(email)) {
            return R.ok().message("重置邮件已发送");
        } else {
            return R.fail().message("该邮箱未注册");
        }
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码")
    public R<?> resetPassword(@Param("email") String email, @Param("code") String code, @Param("newPassword") String newPassword) {

        // 参数校验
        if (email == null || email.isBlank()) {
            return R.fail().message("邮箱不能为空");
        }
        if (code == null || code.length() != 6) {
            return R.fail().message("验证码格式错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return R.fail().message("新密码长度不能少于6位");
        }

        // 调用服务层重置密码（包含验证码验证）
        boolean success = loginService.resetPassword(email, code, newPassword);
        if (success) {
            return R.ok().message("密码重置成功");
        } else {
            return R.fail().message("验证码错误、已过期，或邮箱未注册");
        }
    }

    @GetMapping("/isLogin")
    @Operation(summary = "登录检测")
    public R isLogin(){
        return R.ok().data("isLogin",StpUtil.isLogin());
    }

}
