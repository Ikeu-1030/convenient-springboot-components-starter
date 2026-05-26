package com.ikeu.components.autoconfigure.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ikeu.components.security.context.UserContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus {@link MetaObjectHandler} that auto-fills create/update time
 * and creator/updater fields based on the current security context.
 *
 * <p>Uses {@link MetaObjectHandler#strictInsertFill} and
 * {@link MetaObjectHandler#strictUpdateFill} (MP 3.5.x API), which only fill
 * if the field exists on the entity and the current value is null.
 */
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    private static final Logger log = LoggerFactory.getLogger(AutoFillMetaObjectHandler.class);

    private final MybatisPlusProperties props;

    public AutoFillMetaObjectHandler(MybatisPlusProperties props) {
        this.props = props;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        for (String field : props.getAutoFillCreateFields()) {
            strictInsertFill(metaObject, field, LocalDateTime.class, now);
        }
        String userId = getCurrentUserId();
        if (userId != null) {
            for (String field : props.getAutoFillCreatorFields()) {
                strictInsertFill(metaObject, field, String.class, userId);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        for (String field : props.getAutoFillUpdateFields()) {
            strictUpdateFill(metaObject, field, LocalDateTime.class, now);
        }
        String userId = getCurrentUserId();
        if (userId != null) {
            for (String field : props.getAutoFillUpdaterFields()) {
                strictUpdateFill(metaObject, field, String.class, userId);
            }
        }
    }

    private String getCurrentUserId() {
        try {
            return UserContextHolder.getUserId();
        } catch (Exception e) {
            log.trace("UserContextHolder.getUserId() unavailable: {}", e.getMessage());
            return null;
        }
    }
}
