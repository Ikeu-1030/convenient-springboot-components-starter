package com.ikeu.components.autoconfigure.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for MyBatis-Plus.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link MybatisPlusInterceptor} with {@link PaginationInnerInterceptor}
 *       for automatic pagination</li>
 *   <li>{@link AutoFillMetaObjectHandler} for automatic create/update time
 *       and creator/updater field population</li>
 * </ul>
 * <p>
 * Disabled by default. Enable with {@code ikeu.mybatis-plus.enabled=true}
 * and add {@code mybatis-plus-spring-boot3-starter} to your project.
 *
 * <h3>Configuration</h3>
 * <pre>{@code
 * ikeu:
 *   mybatis-plus:
 *     enabled: true
 *     db-type: mysql
 *     auto-fill-create-fields: [createTime, createdAt]
 *     auto-fill-update-fields: [updateTime, updatedAt]
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(MybatisPlusProperties.class)
@ConditionalOnClass({MybatisPlusInterceptor.class, MetaObjectHandler.class})
@ConditionalOnProperty(prefix = "ikeu.mybatis-plus", name = "enabled", havingValue = "true")
public class MybatisPlusAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MybatisPlusAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusProperties props) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DbType dbType = resolveDbType(props.getDbType());
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(dbType);
        // Set max limit to prevent excessive queries (default: 500)
        paginationInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public AutoFillMetaObjectHandler autoFillMetaObjectHandler(MybatisPlusProperties props) {
        return new AutoFillMetaObjectHandler(props);
    }

    private DbType resolveDbType(String dbType) {
        try {
            return DbType.valueOf(dbType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown db-type '{}', falling back to MYSQL", dbType);
            return DbType.MYSQL;
        }
    }
}
