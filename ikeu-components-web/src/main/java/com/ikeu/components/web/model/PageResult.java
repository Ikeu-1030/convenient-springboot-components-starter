package com.ikeu.components.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Paginated result wrapper. Can be constructed from a MyBatis-Plus Page via reflection.
 *
 * @param <T> record type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private long current;
    private long pages;
    private List<T> records;

    /**
     * Create PageResult from a MyBatis-Plus Page object via reflection (no hard dependency).
     */
    public static <T> PageResult<T> of(Object mybatisPlusPage, List<T> records) {
        if (mybatisPlusPage == null) {
            return PageResult.<T>builder()
                    .total(0).current(1).pages(0)
                    .records(records != null ? records : Collections.emptyList())
                    .build();
        }
        try {
            long total = ((Number) invokeGetter(mybatisPlusPage, "getTotal")).longValue();
            long current = ((Number) invokeGetter(mybatisPlusPage, "getCurrent")).longValue();
            long pages = ((Number) invokeGetter(mybatisPlusPage, "getPages")).longValue();

            return PageResult.<T>builder()
                    .total(total)
                    .current(current)
                    .pages(pages)
                    .records(records != null ? records : Collections.emptyList())
                    .build();
        } catch (Exception e) {
            return PageResult.<T>builder()
                    .total(0).current(1).pages(0)
                    .records(records != null ? records : Collections.emptyList())
                    .build();
        }
    }

    private static Object invokeGetter(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
    }
}
