package com.ikeu.components.autoconfigure.protection;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Spring Security common protections.
 * <p>
 * Prefix: {@code ikeu.security-protection}
 */
@ConfigurationProperties(prefix = "ikeu.security-protection")
public class SecurityProtectionProperties {

    /** Enable Spring Security protection auto-configuration. Default: false. */
    private boolean enabled = false;

    /** Disable CSRF (recommended for JWT-based REST APIs). */
    private boolean csrfDisabled = true;

    /** Use stateless session management (recommended for JWT). */
    private boolean statelessSession = true;

    /** Apply security headers (XSS-protection, frame-options, HSTS, etc.). */
    private boolean securityHeaders = true;

    /** URL patterns that always permit access. */
    private String[] permitAllPatterns = {};

    // ── Getters / Setters ──

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isCsrfDisabled() { return csrfDisabled; }
    public void setCsrfDisabled(boolean csrfDisabled) { this.csrfDisabled = csrfDisabled; }

    public boolean isStatelessSession() { return statelessSession; }
    public void setStatelessSession(boolean statelessSession) { this.statelessSession = statelessSession; }

    public boolean isSecurityHeaders() { return securityHeaders; }
    public void setSecurityHeaders(boolean securityHeaders) { this.securityHeaders = securityHeaders; }

    public String[] getPermitAllPatterns() { return permitAllPatterns; }
    public void setPermitAllPatterns(String[] permitAllPatterns) { this.permitAllPatterns = permitAllPatterns; }
}
