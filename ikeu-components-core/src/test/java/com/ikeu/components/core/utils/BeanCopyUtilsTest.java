package com.ikeu.components.core.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeanCopyUtilsTest {

    @Test
    void copyProperties_basic() {
        Person src = new Person();
        src.setName("John");
        src.setAge(30);
        Person tgt = new Person();
        BeanCopyUtils.copyProperties(src, tgt);
        assertEquals("John", tgt.getName());
        assertEquals(30, tgt.getAge());
    }

    @Test
    void copyProperties_nullExcluded() {
        Person src = new Person();
        src.setName("John");
        // age left as default int=0, which is non-null in Java — will be copied
        Person tgt = new Person();
        tgt.setName("Original");
        tgt.setAge(25);
        BeanCopyUtils.copyProperties(src, tgt);
        assertEquals("John", tgt.getName()); // overwritten
        assertEquals(0, tgt.getAge());       // int default (0) is non-null, so it's copied
    }

    @Test
    void copyProperties_nullSource() {
        Person tgt = new Person();
        tgt.setName("Original");
        BeanCopyUtils.copyProperties(null, tgt);
        assertEquals("Original", tgt.getName()); // unchanged
    }

    @Test
    void copyProperties_nullTarget() {
        Person src = new Person();
        src.setName("John");
        BeanCopyUtils.copyProperties(src, null); // no exception
    }

    @Test
    void copyProperties_nullExcluded_objectProperty() {
        Person src = new Person();
        src.setName("John");
        src.setAddress(new Address());
        src.getAddress().setCity(null); // null city
        Person tgt = new Person();
        tgt.setAddress(new Address());
        tgt.getAddress().setCity("OldCity");
        BeanCopyUtils.copyProperties(src, tgt);
        assertEquals("OldCity", tgt.getAddress().getCity()); // NOT overwritten
    }

    @Test
    void copyProperties_nestedObject() {
        Person src = new Person();
        src.setName("John");
        src.setAddress(new Address());
        src.getAddress().setCity("Beijing");
        src.getAddress().setStreet("Chang'an Ave");
        Person tgt = new Person();
        BeanCopyUtils.copyProperties(src, tgt);
        assertNotNull(tgt.getAddress());
        assertEquals("Beijing", tgt.getAddress().getCity());
        assertEquals("Chang'an Ave", tgt.getAddress().getStreet());
    }

    @Test
    void copyProperties_defaultDepth() {
        // Level 1 → Level 2 → Level 3 → Level 4 (depth 4)
        Person src = new Person();
        src.setName("Level1");
        src.setAddress(new Address());
        src.getAddress().setCity("Level2");
        // Default depth is 3, so up to level 4 (0-indexed: 0..3) should be fine
        Person tgt = new Person();
        BeanCopyUtils.copyProperties(src, tgt);
        assertNotNull(tgt.getAddress());
        assertEquals("Level2", tgt.getAddress().getCity());
    }

    @Test
    void copyProperties_customDepth() {
        Person src = new Person();
        src.setName("Level1");
        src.setAddress(new Address());
        src.getAddress().setCity("Level2");
        Person tgt = new Person();
        BeanCopyUtils.copyProperties(src, tgt, 1);
        assertEquals("Level1", tgt.getName());
        assertNotNull(tgt.getAddress());
        assertEquals("Level2", tgt.getAddress().getCity());
    }

    @Test
    void copyList_basic() {
        List<Person> srcList = new ArrayList<>();
        Person p1 = new Person();
        p1.setName("John");
        p1.setAge(25);
        Person p2 = new Person();
        p2.setName("Jane");
        p2.setAge(30);
        srcList.add(p1);
        srcList.add(p2);

        List<Person> tgtList = BeanCopyUtils.copyList(srcList, Person.class);
        assertEquals(2, tgtList.size());
        assertEquals("John", tgtList.get(0).getName());
        assertEquals("Jane", tgtList.get(1).getName());
    }

    @Test
    void copyList_nullInput() {
        List<Person> result = BeanCopyUtils.copyList(null, Person.class);
        assertTrue(result.isEmpty());
    }

    @Test
    void mergeProperties_basic() {
        Person src = new Person();
        src.setName("John");
        Person tgt = new Person();
        tgt.setAge(30);
        BeanCopyUtils.mergeProperties(src, tgt);
        assertEquals("John", tgt.getName());
        assertEquals(0, tgt.getAge()); // int default 0 from source overwrites target's 30
    }

    // ── Test beans ──

    @SuppressWarnings("unused")
    public static class Person {
        private String name;
        private int age;
        private Address address;

        public Person() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
    }

    @SuppressWarnings("unused")
    public static class Address {
        private String city;
        private String street;

        public Address() {}
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
    }
}