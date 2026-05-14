package com.finance.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 分页插件配置
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 的插件拦截器
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //创建一个拦截器对象
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 关键：注册分页插件,添加「MySQL 分页插件」,让 MyBatis-Plus 自动拼接分页 SQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
