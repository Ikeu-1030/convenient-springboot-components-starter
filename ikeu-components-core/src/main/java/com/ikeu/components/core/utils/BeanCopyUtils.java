package com.ikeu.components.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean property copy with null-exclusion, depth-limited nested copy, and list copy.
 * <p>
 * Uses {@code java.beans.Introspector} for property discovery, with
 * {@link ConcurrentHashMap} caching for performance. Cycle detection via
 * {@link IdentityHashMap}.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Copy non-null properties (merge)
 * BeanCopyUtils.copyProperties(source, target);          // default depth 3
 * BeanCopyUtils.copyProperties(source, target, 5);      // explicit depth
 *
 * // Deep copy a list
 * List<UserVo> vos = BeanCopyUtils.copyList(entities, UserVo.class);
 *
 * // Merge non-null properties (alias)
 * BeanCopyUtils.mergeProperties(partial, existing);
 * }</pre>
 *
 * <h3>Caveats</h3>
 * <ul>
 *   <li><b>Collection properties:</b> the source collection reference is assigned
 *       directly (shallow copy). Source and target will share the same Collection
 *       instance — mutations to one affect the other.</li>
 *   <li><b>Null exclusion:</b> only non-null source properties are copied — this is
 *       merge semantics, not a full overwrite.</li>
 *   <li><b>Nested objects:</b> copied recursively up to the configured depth
 *       (default 3). Beyond that depth, nested objects are skipped.</li>
 * </ul>
 *
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public final class BeanCopyUtils {

    private static final Map<Class<?>, PropertyDescriptor[]> DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    private BeanCopyUtils() {
    }

    /**
     * Copy non-null properties from source to target.
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, 3);
    }

    /**
     * Copy non-null properties from source to target with a depth limit to prevent infinite loops.
     * Depth 1 means only direct properties; deeper levels follow nested beans.
     */
    public static void copyProperties(Object source, Object target, int maxDepth) {
        if (source == null || target == null) {
            return;
        }
        doCopy(source, target, maxDepth, 0, new IdentityHashMap<>());
    }

    @SuppressWarnings("unchecked")
    private static void doCopy(Object source, Object target, int maxDepth, int currentDepth,
                               IdentityHashMap<Object, Object> visited) {
        if (currentDepth > maxDepth || visited.containsKey(source)) {
            return;
        }
        visited.put(source, target);

        PropertyDescriptor[] descriptors = getDescriptors(source.getClass());
        PropertyDescriptor[] targetDescriptors = getDescriptors(target.getClass());
        Map<String, PropertyDescriptor> targetMap = new HashMap<>();
        for (PropertyDescriptor td : targetDescriptors) {
            if (td.getWriteMethod() != null) {
                targetMap.put(td.getName(), td);
            }
        }

        for (PropertyDescriptor sd : descriptors) {
            Method getter = sd.getReadMethod();
            if (getter == null) {
                continue;
            }
            PropertyDescriptor td = targetMap.get(sd.getName());
            if (td == null || td.getWriteMethod() == null) {
                continue;
            }
            try {
                Object value = getter.invoke(source);
                if (value == null) {
                    continue;
                }
                Class<?> type = sd.getPropertyType();
                if (isSimpleType(type)) {
                    td.getWriteMethod().invoke(target, value);
                } else if (value instanceof Collection) {
                    td.getWriteMethod().invoke(target, value);
                } else {
                    // Nested bean: create or reuse target instance
                    Object nestedTarget = td.getReadMethod().invoke(target);
                    if (nestedTarget == null) {
                        nestedTarget = type.getDeclaredConstructor().newInstance();
                        td.getWriteMethod().invoke(target, nestedTarget);
                    }
                    doCopy(value, nestedTarget, maxDepth, currentDepth + 1, visited);
                }
            } catch (Exception e) {
                log.debug("Failed to copy property '{}': {}", sd.getName(), e.getMessage());
            }
        }
    }

    /**
     * Deep copy a list by creating new target instances for each source element.
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            try {
                T target = targetClass.getDeclaredConstructor().newInstance();
                copyProperties(source, target);
                result.add(target);
            } catch (Exception e) {
                log.error("Failed to copy list element: {}", e.getMessage());
            }
        }
        return result;
    }

    /**
     * Merge non-null properties from source into target.
     */
    public static void mergeProperties(Object source, Object target) {
        copyProperties(source, target);
    }

    private static PropertyDescriptor[] getDescriptors(Class<?> clazz) {
        return DESCRIPTOR_CACHE.computeIfAbsent(clazz, c -> {
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(c);
                return beanInfo.getPropertyDescriptors();
            } catch (Exception e) {
                log.warn("Failed to introspect bean: {}", c.getName());
                return new PropertyDescriptor[0];
            }
        });
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.getName().startsWith("java.lang.")
                || type.getName().startsWith("java.math.")
                || type.getName().startsWith("java.time.")
                || type.getName().startsWith("java.util.")
                || type.isEnum();
    }
}
