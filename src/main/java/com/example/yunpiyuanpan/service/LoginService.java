package com.example.yunpiyuanpan.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yunpiyuanpan.mapper.YPEmailregistMapper;
import com.example.yunpiyuanpan.mapper.YPUserMapper;
import com.example.yunpiyuanpan.pojo.YPEmailRegist;
import com.example.yunpiyuanpan.pojo.YPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class LoginService {

    //引入Spring mail依赖之后，会自动装配到IOC容器中
    @Autowired(required = false)
    private JavaMailSender sender;

    @Autowired
    private YPEmailregistMapper ypEmailregistMapper;

    @Autowired
    private YPUserMapper ypUserMapper;

    //登录
    //检查账号密码是否正确
    //允许用户使用邮箱+密码、手机号+密码的形式登录
    public boolean isLoginLegal(String account,String password){
        //创建条件构造器
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("email",account);//根据邮箱
        wrapper.or();
        wrapper.eq("phone",account);//用户名
        YPUser ypUser = ypUserMapper.selectOne(wrapper);
        System.out.println(ypUser);
        if(ypUser==null){
            return false;
        }
        if(password.equals(ypUser.getPassword())){
            return true;
        }
        else{
            return false;
        }

    }

    // 随机生成6位验证码的函数
    private String achieveCode() {
        String[] beforeShuffle= new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
                "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a",
                "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z" };
        List list = Arrays.asList(beforeShuffle);
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
        }
        String afterShuffle = sb.toString();
        String result = afterShuffle.substring(3, 9);
        System.out.print(result);
        return result;
    }

    /**
     * 发送密码重置验证码到指定邮箱
     * @param email 用户注册邮箱
     * @return 是否发送成功
     */
    public boolean sendPasswordResetEmail(String email) {
        // 1. 先检查该邮箱是否已注册用户
        QueryWrapper<YPUser> userWrapper = new QueryWrapper<>();
        userWrapper.eq("email", email);
        if (ypUserMapper.selectOne(userWrapper) == null) {
            System.out.println("该邮箱未注册，无法发送重置邮件");
            return false; // 或抛出异常
        }

        // 2. 生成6位验证码
        String code = achieveCode();

        // 3. 构建邮件内容
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("【云批原盘】密码重置验证码");
        message.setText("您正在尝试重置密码，验证码为：" + code + "，5分钟内有效。如非本人操作，请忽略此邮件。");
        message.setTo(email);
        message.setFrom("809464456@qq.com"); // 确保这个邮箱已配置 SMTP

        try {
            sender.send(message);
            System.out.println("重置验证码邮件已发送至：" + email);
        } catch (Exception e) {
            System.err.println("邮件发送失败：" + e.getMessage());
            return false;
        }

        // 4. 存储验证码到 yp_emailregist 表（复用）
        // 注意：这里和注册共用一张表，后续可通过业务约定区分（如：注册后立即删除，重置后也删除）
        YPEmailRegist existing = ypEmailregistMapper.selectByEmail(email);
        if (existing == null) {
            ypEmailregistMapper.insertOne(email, code);
        } else {
            // 覆盖旧验证码（无论是注册还是重置）
            ypEmailregistMapper.deleteOne(email);
            ypEmailregistMapper.insertOne(email, code);
        }

        return true;
    }

    //在这个方法里，先验证code是否过期，再验证email和code是否能匹配的上
    public boolean isCodeLegal(String email,String code){//测试通过

        YPEmailRegist ypEmailRegist = new YPEmailRegist();
        ypEmailRegist = ypEmailregistMapper.selectByEmail(email);
        //如果code过期了，返回false
        try{
            Date codeDate = null;
            codeDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(ypEmailRegist.getCreateTime());
//            System.out.println(codeDate.getTime());
            Date nowDate = new Date();
//            System.out.println(nowDate.getTime());
            long result = nowDate.getTime() - codeDate.getTime();
//            System.out.println(result);
            long oneMinute = 60000;
//            System.out.println(result/oneMinute);
            if(result/oneMinute > 5 ){//已经过了5分钟
                System.out.println("验证码已过期");
                return false;
            }
            System.out.println("验证码未过期");
        }catch (ParseException e){
            e.printStackTrace();
        }
        //执行到这说明还未过期，可以进行比对
        if(code.equals(ypEmailRegist.getCode())){//如果相同
            System.out.println("验证码正确");
            return true;
        }else{
            System.out.println("验证码错误");
            return false;
        }

    }

    // RegistService.java

    /**
     * 验证验证码并重置用户密码
     *
     * @param email        用户注册邮箱
     * @param code         验证码
     * @param newPassword  新密码（建议前端传 MD5 或后端加密）
     * @return 是否重置成功
     */
    public boolean resetPassword(String email, String code, String newPassword) {
        // 1. 验证验证码是否合法（存在、未过期、匹配）
        if (!isCodeLegal(email, code)) {
            System.out.println("验证码无效或已过期");
            return false;
        }

        // 2. 查询用户是否存在（双重保险）
        QueryWrapper<YPUser> userWrapper = new QueryWrapper<>();
        userWrapper.eq("email", email);
        YPUser user = ypUserMapper.selectOne(userWrapper);
        if (user == null) {
            System.out.println("该邮箱未注册用户");
            return false;
        }

        // 3. 更新密码（⚠️ 注意：生产环境应加密存储！）
        user.setPassword(newPassword); // 如果前端传的是明文，这里建议加密
        int updated = ypUserMapper.updateById(user);

        if (updated <= 0) {
            System.err.println("密码更新失败");
            return false;
        }

        // 4. 删除验证码（确保一次性使用）
        ypEmailregistMapper.deleteOne(email);
        System.out.println("密码重置成功，验证码已清除");

        return true;
    }
}
