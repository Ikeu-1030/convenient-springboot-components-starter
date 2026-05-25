package com.ikeu.components.core.convert;

import com.ikeu.components.core.utils.BeanCopyUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeanConverterTest {

    @Test
    void directCopyWorks() {
        UserEntity entity = new UserEntity();
        entity.setName("john");
        entity.setId(100L);
        UserVo vo = new UserVo();
        BeanCopyUtils.copyProperties(entity, vo);
        assertEquals("john", vo.getName(), "Direct BeanCopyUtils should copy name");
        assertEquals(100L, vo.getId(), "Direct BeanCopyUtils should copy id");
    }

    @Test
    void convert_basic() {
        // Only set name (String), skip id (Long) to test basic copy
        UserEntity entity = new UserEntity();
        entity.setName("john");
        UserVo vo = BeanConverter.convert(entity, UserVo.class);
        assertNotNull(vo);
        assertEquals("john", vo.getName());
    }

    @Test
    void convert_withId() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setName("john");
        UserVo vo = BeanConverter.convert(entity, UserVo.class);
        assertNotNull(vo);
        assertEquals("john", vo.getName());
        assertEquals(1L, vo.getId());
    }

    @Test
    void convert_nullInput() {
        assertNull(BeanConverter.convert(null, UserVo.class));
    }

    @Test
    void convert_sameTypeReturnsDirectly() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        UserEntity result = BeanConverter.convert(entity, UserEntity.class);
        assertSame(entity, result);
    }

    @Test
    void convert_customConverter() {
        BeanConverter.register(Cat.class, Dog.class, (src, tgt) -> {
            tgt.setSound(src.getSound().toUpperCase());
            return tgt;
        });
        Cat cat = new Cat();
        cat.setSound("meow");
        Dog dog = BeanConverter.convert(cat, Dog.class);
        assertEquals("MEOW", dog.getSound());
    }

    @Test
    void convertList_basic() {
        UserEntity e1 = new UserEntity();
        e1.setId(1L);
        e1.setName("john");
        UserEntity e2 = new UserEntity();
        e2.setId(2L);
        e2.setName("jane");
        List<UserVo> vos = BeanConverter.convertList(Arrays.asList(e1, e2), UserVo.class);
        assertEquals(2, vos.size());
        assertEquals("john", vos.get(0).getName());
        assertEquals("jane", vos.get(1).getName());
    }

    @Test
    void convertList_nullInput() {
        List<UserVo> result = BeanConverter.convertList(null, UserVo.class);
        assertTrue(result.isEmpty());
    }

    @Test
    void combine_multiSource() {
        UserEntity e1 = new UserEntity();
        e1.setName("john");
        UserEntity e2 = new UserEntity();
        e2.setId(100L);
        UserVo vo = BeanConverter.combine(UserVo.class, e1, e2);
        assertNotNull(vo);
        assertEquals("john", vo.getName());
        assertEquals(100L, vo.getId());
    }

    @Test
    void combine_emptySources() {
        assertNull(BeanConverter.combine(UserVo.class, (Object[]) null));
        assertNull(BeanConverter.combine(UserVo.class));
    }

    @Test
    void combine_withCallback() {
        UserEntity e1 = new UserEntity();
        e1.setName("john");
        UserVo vo = BeanConverter.combine(UserVo.class, tgt -> {
            tgt.setName("PREFIX_" + tgt.getName());
        }, e1);
        assertEquals("PREFIX_john", vo.getName());
    }

    // ── Test beans ──

    @SuppressWarnings("unused")
    public static class UserEntity {
        private Long id;
        private String name;

        public UserEntity() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @SuppressWarnings("unused")
    public static class UserVo {
        private Long id;
        private String name;

        public UserVo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @SuppressWarnings("unused")
    public static class Cat {
        private String sound;
        public Cat() {}
        public String getSound() { return sound; }
        public void setSound(String sound) { this.sound = sound; }
    }

    @SuppressWarnings("unused")
    public static class Dog {
        private String sound;
        public Dog() {}
        public String getSound() { return sound; }
        public void setSound(String sound) { this.sound = sound; }
    }
}