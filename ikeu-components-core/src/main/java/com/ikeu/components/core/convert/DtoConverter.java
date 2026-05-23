package com.ikeu.components.core.convert;

import com.ikeu.components.core.utils.BeanCopyUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Generic DTO/Entity/VO converter with reflection-based copying and support for custom mappings.
 */
@Slf4j
public final class DtoConverter {

    private static final Map<ConverterKey, BiFunction<?, ?, ?>> CUSTOM_CONVERTERS = new ConcurrentHashMap<>();

    private DtoConverter() {
    }

    /**
     * Register a custom converter for a specific source-target pair.
     */
    @SuppressWarnings("unchecked")
    public static <S, T> void register(Class<S> sourceClass, Class<T> targetClass,
                                       BiFunction<S, T, T> converter) {
        CUSTOM_CONVERTERS.put(new ConverterKey(sourceClass, targetClass),
                (BiFunction<? super Object, ? super Object, ?>) converter);
    }

    /**
     * Convert a single object to the target class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        if (targetClass.isInstance(source)) {
            return targetClass.cast(source);
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            // Check for custom converter
            ConverterKey key = new ConverterKey(source.getClass(), targetClass);
            @SuppressWarnings("rawtypes")
            BiFunction custom = CUSTOM_CONVERTERS.get(key);
            if (custom != null) {
                return (T) custom.apply(source, target);
            }
            BeanCopyUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("Failed to convert {} to {}", source.getClass().getName(),
                    targetClass.getName(), e);
            throw new RuntimeException("Conversion failed", e);
        }
    }

    /**
     * Convert a list of objects to the target class.
     */
    public static <T> List<T> convertList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>(sourceList.size());
        for (Object source : sourceList) {
            result.add(convert(source, targetClass));
        }
        return result;
    }

    /**
     * Convert a MyBatis-Plus Page object via reflection (no hard dependency on MyBatis-Plus).
     *
     * @param page        MyBatis-Plus IPage or Page object
     * @param targetClass target record class
     * @param <T>         record type
     * @return map with keys: "records", "total", "current", "pages"
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, Object> convertPage(Object page, Class<T> targetClass) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (page == null) {
            return result;
        }
        try {
            MethodHelper helper = new MethodHelper(page.getClass());

            List<?> records = (List<?>) helper.invokeGetter(page, "getRecords");
            result.put("records", records != null ? convertList(records, targetClass) : Collections.emptyList());

            result.put("total", helper.invokeGetter(page, "getTotal"));
            result.put("current", helper.invokeGetter(page, "getCurrent"));
            result.put("pages", helper.invokeGetter(page, "getPages"));
        } catch (Exception e) {
            log.error("Failed to convert MyBatis-Plus page", e);
        }
        return result;
    }

    /**
     * Lightweight reflective method helper. Avoids full reflection library dependency.
     */
    private static class MethodHelper {
        private final Map<String, java.lang.reflect.Method> methods = new HashMap<>();

        MethodHelper(Class<?> clazz) {
            for (java.lang.reflect.Method m : clazz.getMethods()) {
                methods.put(m.getName(), m);
            }
        }

        Object invokeGetter(Object target, String name) {
            java.lang.reflect.Method m = methods.get(name);
            if (m == null) {
                return null;
            }
            try {
                return m.invoke(target);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static class ConverterKey {
        private final Class<?> source;
        private final Class<?> target;

        ConverterKey(Class<?> source, Class<?> target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ConverterKey that)) return false;
            return source.equals(that.source) && target.equals(that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }
    }
}
