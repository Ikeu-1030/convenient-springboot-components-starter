package com.ikeu.components.autoconfigure.mybatis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for MyBatis-Plus integration.
 * <p>
 * Prefix: {@code ikeu.mybatis-plus}
 */
@ConfigurationProperties(prefix = "ikeu.mybatis-plus")
public class MybatisPlusProperties {

    /** Enable MyBatis-Plus auto-configuration. Default: false (opt-in). */
    private boolean enabled = false;

    /** Database type for the pagination plugin. */
    private String dbType = "mysql";

    /** Field names to auto-fill with current time on insert. */
    private List<String> autoFillCreateFields = Arrays.asList("createTime", "createdAt", "gmtCreate");

    /** Field names to auto-fill with current time on update. */
    private List<String> autoFillUpdateFields = Arrays.asList("updateTime", "updatedAt", "gmtUpdate");

    /** Field names to auto-fill with current user ID on insert. */
    private List<String> autoFillCreatorFields = Arrays.asList("creator", "createBy");

    /** Field names to auto-fill with current user ID on update. */
    private List<String> autoFillUpdaterFields = Arrays.asList("updater", "updateBy");

    // ── Getters / Setters ──

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }

    public List<String> getAutoFillCreateFields() { return autoFillCreateFields; }
    public void setAutoFillCreateFields(List<String> autoFillCreateFields) { this.autoFillCreateFields = autoFillCreateFields; }

    public List<String> getAutoFillUpdateFields() { return autoFillUpdateFields; }
    public void setAutoFillUpdateFields(List<String> autoFillUpdateFields) { this.autoFillUpdateFields = autoFillUpdateFields; }

    public List<String> getAutoFillCreatorFields() { return autoFillCreatorFields; }
    public void setAutoFillCreatorFields(List<String> autoFillCreatorFields) { this.autoFillCreatorFields = autoFillCreatorFields; }

    public List<String> getAutoFillUpdaterFields() { return autoFillUpdaterFields; }
    public void setAutoFillUpdaterFields(List<String> autoFillUpdaterFields) { this.autoFillUpdaterFields = autoFillUpdaterFields; }
}
