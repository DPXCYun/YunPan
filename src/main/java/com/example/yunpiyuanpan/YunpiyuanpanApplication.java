package com.example.yunpiyuanpan;

import cn.dev33.satoken.SaManager;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("com.example.yunpiyuanpan.mapper") // 👈 添加这行！
public class YunpiyuanpanApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunpiyuanpanApplication.class, args);
        log.info("启动成功：Sa-Token配置如下：" + SaManager.getConfig());
    }

}
