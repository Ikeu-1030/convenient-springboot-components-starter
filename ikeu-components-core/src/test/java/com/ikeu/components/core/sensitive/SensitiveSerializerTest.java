package com.ikeu.components.core.sensitive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveSerializerTest {

    private final ObjectMapper mapper = createMapper();

    private static ObjectMapper createMapper() {
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule("test");
        module.addSerializer(String.class, new SensitiveSerializer());
        m.registerModule(module);
        return m;
    }

    @Test
    void phone_shouldMaskMiddleFourDigits() throws Exception {
        User user = new User("13812345678");
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("138****5678"), "Expected masked phone, got: " + json);
    }

    @Test
    void chineseName_shouldKeepFirstChar() throws Exception {
        NameRecord record = new NameRecord("张三丰");
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("\"张**\""), "Expected masked name, got: " + json);
    }

    @Test
    void idCard_shouldMaskMiddle() throws Exception {
        IdCardRecord record = new IdCardRecord("110101199001011234");
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("110***********1234"), "Expected masked ID, got: " + json);
    }

    @Test
    void email_shouldMaskLocalPart() throws Exception {
        EmailRecord record = new EmailRecord("test@example.com");
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("t***@example.com"), "Expected masked email, got: " + json);
    }

    @Test
    void bankCard_shouldMaskMiddle() throws Exception {
        BankCardRecord record = new BankCardRecord("6222021234561234");
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("6222********1234"), "Expected masked bank card, got: " + json);
    }

    @Test
    void address_shouldKeepFirstSix() throws Exception {
        AddressRecord record = new AddressRecord("北京市海淀区中关村大街1号");
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("北京市海淀区"), "Expected masked address, got: " + json);
        int maskedCount = json.length() - json.indexOf("北京市海淀区") - 6;
        assertTrue(maskedCount > 0, "Should have masked characters after first 6");
    }

    @Test
    void password_shouldMaskAll() throws Exception {
        PasswordRecord record = new PasswordRecord("mySecret123");
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("\"***********\""), "Expected fully masked, got: " + json);
    }

    @Test
    void custom_shouldRespectIncludeRanges() throws Exception {
        CustomRecord record = new CustomRecord("ABCDEFGH");
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("\"AB****GH\""), "Expected custom masked, got: " + json);
    }

    @Test
    void nullValue_shouldWriteNull() throws Exception {
        User user = new User(null);
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"phone\":null"), "Expected null, got: " + json);
    }

    @Test
    void emptyValue_shouldWriteEmpty() throws Exception {
        User user = new User("");
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"phone\":\"\""), "Expected empty, got: " + json);
    }

    @Test
    void unannotatedField_shouldNotBeAffected() throws Exception {
        NormalRecord record = new NormalRecord("hello");
        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("\"normal\":\"hello\""), "Expected unchanged, got: " + json);
    }

    // ── Test beans ──

    public static class User {
        @Sensitive(SensitiveType.PHONE)
        private String phone;
        public User() {}
        public User(String phone) { this.phone = phone; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class NameRecord {
        @Sensitive(SensitiveType.CHINESE_NAME)
        private String name;
        public NameRecord() {}
        public NameRecord(String name) { this.name = name; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class IdCardRecord {
        @Sensitive(SensitiveType.ID_CARD)
        private String idCard;
        public IdCardRecord() {}
        public IdCardRecord(String idCard) { this.idCard = idCard; }
        public String getIdCard() { return idCard; }
        public void setIdCard(String idCard) { this.idCard = idCard; }
    }

    public static class EmailRecord {
        @Sensitive(SensitiveType.EMAIL)
        private String email;
        public EmailRecord() {}
        public EmailRecord(String email) { this.email = email; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class BankCardRecord {
        @Sensitive(SensitiveType.BANK_CARD)
        private String bankCard;
        public BankCardRecord() {}
        public BankCardRecord(String bankCard) { this.bankCard = bankCard; }
        public String getBankCard() { return bankCard; }
        public void setBankCard(String bankCard) { this.bankCard = bankCard; }
    }

    public static class AddressRecord {
        @Sensitive(SensitiveType.ADDRESS)
        private String address;
        public AddressRecord() {}
        public AddressRecord(String address) { this.address = address; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class PasswordRecord {
        @Sensitive(SensitiveType.PASSWORD)
        private String password;
        public PasswordRecord() {}
        public PasswordRecord(String password) { this.password = password; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class CustomRecord {
        @Sensitive(value = SensitiveType.CUSTOM, startInclude = 2, endInclude = 2)
        private String value;
        public CustomRecord() {}
        public CustomRecord(String value) { this.value = value; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class NormalRecord {
        private String normal;
        public NormalRecord() {}
        public NormalRecord(String normal) { this.normal = normal; }
        public String getNormal() { return normal; }
        public void setNormal(String normal) { this.normal = normal; }
    }
}
