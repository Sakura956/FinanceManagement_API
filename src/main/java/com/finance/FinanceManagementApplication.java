package com.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// MyBatis-Plus 扫描 Mapper 接口
@MapperScan("com.finance.modules.*.mapper")
public class FinanceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceManagementApplication.class, args);

        //生成初始管理员密码测试
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String encoded = encoder.encode("admin123");
//        System.out.println(encoded);
    }

}
