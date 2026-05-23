package com.ikeu.components.core.convert;

import com.ikeu.components.core.utils.BeanCopyUtils;
import lombok.extern.slf4j.Slf4j;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Generic bean converter supporting Entity/DTO/VO conversion, custom mappings,
 * list/page conversion, and multi-source object merging.
 */
@Slf4j
public final class BeanConverter {

    private static final Map<ConverterKey, BiFunction<?, ?, ?>> CUSTOM_CONVERTERS = new ConcurrentHashMap<>();

    private BeanConverter() {
    }

    /**
     * Register a custom converter for a specific source-target type pair.
     * The converter receives the source and a pre-created target instance,
     * and should return the populated target.
     */
    public static <S, T> void register(Class<S> sourceClass, Class<T> targetClass,
                                       BiFunction<S, T, T> converter) {
        CUSTOM_CONVERTERS.put(new ConverterKey(sourceClass, targetClass), converter);
    }

    /**
     * Convert a single object to the target class.
     * If source is already an instance of targetClass, returns it directly.
     * Custom registered converters take precedence over automatic property copying.
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        if (targetClass.isInstance(source)) {
            return targetClass.cast(source);
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();

            ConverterKey key = new ConverterKey(source.getClass(), targetClass);
            BiFunction<?, ?, ?> custom = CUSTOM_CONVERTERS.get(key);
            if (custom != null) {
                @SuppressWarnings("unchecked")
                BiFunction<Object, T, T> typed = (BiFunction<Object, T, T>) custom;
                return typed.apply(source, target);
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
     * Convert a list of objects to the target type.
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
     * Convert a MyBatis-Plus IPage/Page object to a map with converted records.
     * Uses reflection so MyBatis-Plus is not a hard dependency.
     *
     * @param page        MyBatis-Plus Page object
     * @param targetClass target record type
     * @return map with keys: records, total, current, pages
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, Object> convertPage(Object page, Class<T> targetClass) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (page == null) {
            return result;
        }
        try {
            PropertyDescriptor[] descriptors = getDescriptors(page.getClass());
            Map<String, PropertyDescriptor> descMap = new HashMap<>();
            for (PropertyDescriptor pd : descriptors) {
                if (pd.getReadMethod() != null) {
                    descMap.put(pd.getName(), pd);
                }
            }

            List<?> records = (List<?>) readProperty(page, descMap.get("records"));
            result.put("records", records != null ? convertList(records, targetClass) : Collections.emptyList());
            result.put("total", readProperty(page, descMap.get("total")));
            result.put("current", readProperty(page, descMap.get("current")));
            result.put("pages", readProperty(page, descMap.get("pages")));
        } catch (Exception e) {
            log.error("Failed to convert MyBatis-Plus page", e);
        }
        return result;
    }

    /**
     * Merge non-null properties from multiple source objects into a new target instance.
     * Sources are processed in order; later non-null values overwrite earlier ones.
     *
     * @param targetClass target type
     * @param sources     one or more source objects
     * @return populated target instance, or null if sources is empty/null
     */
    public static <T> T combine(Class<T> targetClass, Object... sources) {
        if (sources == null || sources.length == 0) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            for (Object source : sources) {
                if (source != null) {
                    BeanCopyUtils.copyProperties(source, target);
                }
            }
            return target;
        } catch (Exception e) {
            log.error("BeanConverter combine failed for target {}", targetClass.getName(), e);
            throw new RuntimeException("BeanConverter combine failed", e);
        }
    }

    /**
     * Merge multiple sources into a new target, then apply a post-processing callback.
     *
     * @param targetClass  target type
     * @param afterCombine callback invoked after merging all sources (ignored if null)
     * @param sources      one or more source objects
     * @return populated target instance
     */
    public static <T> T combine(Class<T> targetClass, Consumer<T> afterCombine, Object... sources) {
        T target = combine(targetClass, sources);
        if (target != null && afterCombine != null) {
            afterCombine.accept(target);
        }
        return target;
    }

    private static Object readProperty(Object bean, PropertyDescriptor pd) {
        if (pd == null || pd.getReadMethod() == null) {
            return null;
        }
        try {
            return pd.getReadMethod().invoke(bean);
        } catch (Exception e) {
            return null;
        }
    }

    private static PropertyDescriptor[] getDescriptors(Class<?> clazz) {
        try {
            return Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        } catch (Exception e) {
            log.warn("Failed to introspect bean: {}", clazz.getName());
            return new PropertyDescriptor[0];
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