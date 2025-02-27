package com.zhihu.fust.spring.toolkit.beans;

import lombok.Data;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanCopyUtilsTest {

    private SourceBean source;
    private TargetBean target;

    @BeforeEach
    void setUp() {
        source = Instancio.create(SourceBean.class);
        source.setField5(1000);
        source.setField6("1010");
        source.setField7("" + Long.MAX_VALUE);
        target = new TargetBean();
    }

    @Test
    void testCopyTo() {
        TargetBean result = BeanCopyUtils.copyTo(source, TargetBean.class);
        assertNotNull(result);
        assetBeanCopyResult(source, result);
    }


    @Test
    void testToType() {
        TargetBean result = BeanCopyUtils.toType(TargetBean.class).apply(source);
        assertNotNull(result);
        assetBeanCopyResult(source, result);
    }


    @Test
    void testCopyProperties() {
        BeanCopyUtils.copyProperties(source, target);
        assetBeanCopyResult(source, target);
    }

    @Test
    void testCopyPropertiesWithIgnore() {
        BeanCopyUtils.copyProperties(source, target, "ignoreField");
        assertNull(target.getIgnoreField());
        assetBeanCopyResult(source, target);
    }

    void assetBeanCopyResult(SourceBean source, TargetBean target) {
        assertEquals(source.getId().toString(), target.getId());
        assertEquals(source.getIntId().toString(), target.getIntId());
        assertEquals(source.getShortId().toString(), target.getShortId());
        assertEquals(source.getStringField(), target.getStringField());
        assertEquals(source.getIntField(), target.getIntField());
        assertEquals(source.getLongField(), target.getLongField());
        assertNull(target.getTargetOnlyField());
    }


    // Helper classes for testing
    @Data
    static class SourceBean {
        private Long id;
        private Integer intId;
        private Short shortId;
        private String stringField;
        private int intField;
        private long longField;
        private String ignoreField;
        private String sourceOnlyField;
        private long field1;
        private int field2;
        private short field3;
        private short field4;
        private int field5;
        private String field6;
        private String field7;
    }

    @Data
    static class TargetBean {
        private String id;
        private String intId;
        private String shortId;
        private String stringField;
        private int intField;
        private long longField;
        private String ignoreField;
        private String targetOnlyField;
        private long field1;
        private long field2;
        private long field3;
        private int field4;
        private short field5;
        private int field6;
        private long field7;
    }
}