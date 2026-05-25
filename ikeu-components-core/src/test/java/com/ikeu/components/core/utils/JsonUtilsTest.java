package com.ikeu.components.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @BeforeEach
    void setUp() {
        JsonUtils.resetObjectMapper();
    }

    // ── Serialization ──

    @Test
    void toJson_basic() {
        TestUser user = new TestUser("John", 25);
        String json = JsonUtils.toJson(user);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"John\""));
        assertTrue(json.contains("\"age\":25"));
    }

    @Test
    void toJson_nullInput() {
        assertNull(JsonUtils.toJson(null));
    }

    @Test
    void toJsonPretty() {
        TestUser user = new TestUser("John", 25);
        String json = JsonUtils.toJsonPretty(user);
        assertNotNull(json);
        assertTrue(json.contains("\n"));
    }

    @Test
    void toJsonBytes() {
        TestUser user = new TestUser("John", 25);
        byte[] bytes = JsonUtils.toJsonBytes(user);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void toJsonBytes_nullInput() {
        assertNull(JsonUtils.toJsonBytes(null));
    }

    // ── Deserialization ──

    @Test
    void fromJson_basic() {
        String json = "{\"name\":\"John\",\"age\":25}";
        TestUser user = JsonUtils.fromJson(json, TestUser.class);
        assertNotNull(user);
        assertEquals("John", user.getName());
        assertEquals(25, user.getAge());
    }

    @Test
    void fromJson_nullString() {
        assertNull(JsonUtils.fromJson(null, TestUser.class));
    }

    @Test
    void fromJson_blankString() {
        assertNull(JsonUtils.fromJson("   ", TestUser.class));
    }

    @Test
    void fromJsonList() {
        String json = "[{\"name\":\"John\"},{\"name\":\"Jane\"}]";
        List<TestUser> users = JsonUtils.fromJsonList(json, TestUser.class);
        assertEquals(2, users.size());
        assertEquals("John", users.get(0).getName());
        assertEquals("Jane", users.get(1).getName());
    }

    @Test
    void fromJsonList_emptyArray() {
        List<TestUser> users = JsonUtils.fromJsonList("[]", TestUser.class);
        assertTrue(users.isEmpty());
    }

    @Test
    void fromJsonList_nullString() {
        List<TestUser> users = JsonUtils.fromJsonList(null, TestUser.class);
        assertTrue(users.isEmpty());
    }

    @Test
    void fromJsonWithTypeReference() {
        String json = "[{\"name\":\"John\"},{\"name\":\"Jane\"}]";
        List<TestUser> users = JsonUtils.fromJson(json,
                new TypeReference<List<TestUser>>() {});
        assertEquals(2, users.size());
    }

    @Test
    void fromJsonWithTypeReference_null() {
        assertNull(JsonUtils.fromJson(null, new TypeReference<List<TestUser>>() {}));
    }

    @Test
    void fromJsonMap() {
        String json = "{\"key1\":\"value1\",\"key2\":123}";
        Map<String, Object> map = JsonUtils.fromJsonMap(json);
        assertEquals("value1", map.get("key1"));
        assertEquals(123, map.get("key2"));
    }

    @Test
    void fromJsonMap_nullString() {
        assertNull(JsonUtils.fromJsonMap(null));
    }

    // ── Long→String serialization ──

    @Test
    void longSerializedAsString() {
        TestUser user = new TestUser("John", 25);
        user.setId(9999999999999999L);
        String json = JsonUtils.toJson(user);
        assertTrue(json.contains("\"id\":\"9999999999999999\""));
    }

    // ── Conversion ──

    @Test
    void convert_basic() {
        TestUser user = new TestUser("John", 25);
        TestUserVo vo = JsonUtils.convert(user, TestUserVo.class);
        assertNotNull(vo);
        assertEquals("John", vo.getName());
        assertEquals(25, vo.getAge());
    }

    @Test
    void convert_nullInput() {
        assertNull(JsonUtils.convert(null, TestUserVo.class));
    }

    @Test
    void convert_withTypeReference() {
        TestUser user = new TestUser("John", 25);
        Map<String, Object> map = JsonUtils.convert(user,
                new TypeReference<Map<String, Object>>() {});
        assertEquals("John", map.get("name"));
        assertEquals(25, map.get("age"));
    }

    // ── Validation ──

    @Test
    void isValidJson_valid() {
        assertTrue(JsonUtils.isValidJson("{\"key\":\"value\"}"));
        assertTrue(JsonUtils.isValidJson("[1,2,3]"));
    }

    @Test
    void isValidJson_invalid() {
        assertFalse(JsonUtils.isValidJson("not-json"));
    }

    @Test
    void isValidJson_null() {
        assertFalse(JsonUtils.isValidJson(null));
    }

    @Test
    void isValidJson_blank() {
        assertFalse(JsonUtils.isValidJson("   "));
    }

    // ── ObjectMapper management ──

    @Test
    void setObjectMapper_customMapper() {
        ObjectMapper custom = new ObjectMapper();
        JsonUtils.setObjectMapper(custom);
        assertSame(custom, JsonUtils.getObjectMapper());
    }

    @Test
    void setObjectMapper_nullDoesNotReplace() {
        ObjectMapper current = JsonUtils.getObjectMapper();
        JsonUtils.setObjectMapper(null);
        assertSame(current, JsonUtils.getObjectMapper());
    }

    @Test
    void resetObjectMapper() {
        ObjectMapper defaultMapper = JsonUtils.getObjectMapper();
        ObjectMapper custom = new ObjectMapper();
        JsonUtils.setObjectMapper(custom);
        JsonUtils.resetObjectMapper();
        assertNotSame(custom, JsonUtils.getObjectMapper());
        // After reset, should be a new default mapper
        assertNotNull(JsonUtils.getObjectMapper());
        assertNotSame(defaultMapper, JsonUtils.getObjectMapper());
    }

    // ── Test POJOs ──

    @SuppressWarnings("unused")
    static class TestUser {
        private Long id;
        private String name;
        private int age;

        TestUser() {}
        TestUser(String name, int age) { this.name = name; this.age = age; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    @SuppressWarnings("unused")
    static class TestUserVo {
        private String name;
        private int age;

        TestUserVo() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}