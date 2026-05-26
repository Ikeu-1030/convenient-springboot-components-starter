package com.ikeu.components.core.sensitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * Jackson {@link ContextualSerializer} that masks String fields annotated with {@link Sensitive}.
 *
 * <p>Registered globally for all String properties. For each property, Jackson calls
 * {@link #createContextual} to check for {@code @Sensitive}; unannotated properties
 * delegate to the default String serializer with zero overhead.
 */
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final Sensitive sensitive;

    /** No-arg constructor required by Jackson for the global registration. */
    public SensitiveSerializer() {
        this.sensitive = null;
    }

    private SensitiveSerializer(Sensitive sensitive) {
        this.sensitive = sensitive;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }
        if (sensitive == null) {
            gen.writeString(value);
            return;
        }
        gen.writeString(mask(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        if (property != null) {
            Sensitive annotation = property.getAnnotation(Sensitive.class);
            if (annotation != null) {
                return new SensitiveSerializer(annotation);
            }
        }
        return this;
    }

    private String mask(String value) {
        SensitiveType type = sensitive.value();
        if (type == SensitiveType.CUSTOM) {
            return applyCustom(value, sensitive.startInclude(), sensitive.endInclude(), sensitive.maskChar());
        }
        return switch (type) {
            case CHINESE_NAME -> maskChineseName(value);
            case ID_CARD -> maskIdCard(value);
            case PHONE -> maskPhone(value);
            case EMAIL -> maskEmail(value);
            case BANK_CARD -> maskBankCard(value);
            case ADDRESS -> maskAddress(value);
            case PASSWORD -> maskPassword(value);
            default -> applyCustom(value, 0, 0, '*');
        };
    }

    private String maskChineseName(String value) {
        if (value.length() <= 1) return value;
        return value.charAt(0) + repeatChar(sensitive.maskChar(), value.length() - 1);
    }

    private String maskIdCard(String value) {
        if (value.length() <= 7) return maskPassword(value);
        return value.substring(0, 3)
                + repeatChar(sensitive.maskChar(), value.length() - 7)
                + value.substring(value.length() - 4);
    }

    private String maskPhone(String value) {
        if (value.length() <= 7) return maskPassword(value);
        return value.substring(0, 3)
                + repeatChar(sensitive.maskChar(), 4)
                + value.substring(value.length() - 4);
    }

    private String maskEmail(String value) {
        int atIndex = value.indexOf('@');
        if (atIndex <= 1) return maskPassword(value);
        return value.charAt(0) + repeatChar(sensitive.maskChar(), atIndex - 1) + value.substring(atIndex);
    }

    private String maskBankCard(String value) {
        if (value.length() <= 8) return maskPassword(value);
        return value.substring(0, 4)
                + repeatChar(sensitive.maskChar(), value.length() - 8)
                + value.substring(value.length() - 4);
    }

    private String maskAddress(String value) {
        if (value.length() <= 6) return maskPassword(value);
        return value.substring(0, 6) + repeatChar(sensitive.maskChar(), value.length() - 6);
    }

    private String maskPassword(String value) {
        return repeatChar(sensitive.maskChar(), value.length());
    }

    private String applyCustom(String value, int startInclude, int endInclude, char maskChar) {
        int len = value.length();
        if (startInclude + endInclude >= len) {
            return value;
        }
        return value.substring(0, startInclude)
                + repeatChar(maskChar, len - startInclude - endInclude)
                + value.substring(len - endInclude);
    }

    private static String repeatChar(char c, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
