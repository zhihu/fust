package com.zhihu.fust.boot.mybatis;

import com.zhihu.fust.boot.mybatis.app.SkuDao;
import com.zhihu.fust.boot.mybatis.app.SkuModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MybatisTestBoot.class)
class MybatisBeanConfigTest {

    @Autowired
    private SkuDao skuDao;

    @Test
    void testSkuDao() throws InterruptedException {
        SkuModel model = new SkuModel();
        model.setName("test1");
        model.setPrice(10);
        skuDao.create(model);

        assertNotNull(model.getId());


        // find by id
        model = skuDao.find(model.getId());

        assertEquals("test1", model.getName());
        assertEquals(10, model.getPrice());
        assertEquals(1, skuDao.count());
        assertNotNull(model.getCreatedAt());
        assertNotNull(model.getUpdatedAt());

        // test find by name
        model = skuDao.findByName("test1");
        assertEquals("test1", model.getName());
        assertEquals(10, model.getPrice());

        // test update
        model.setName("test2");
        skuDao.update(model);
        model = skuDao.findByName("test2");
        assertEquals("test2", model.getName());
        assertEquals(10, model.getPrice());


        // test patch
        SkuModel modelForPatch = new SkuModel();
        modelForPatch.setId(model.getId());
        modelForPatch.setName("test3");
        skuDao.patch(modelForPatch);

        model = skuDao.findByName("test3");
        assertEquals("test3", model.getName());
        assertEquals(10, model.getPrice());
        assertEquals(1, skuDao.count());

        // test batch create
        SkuModel model4 = new SkuModel();
        model4.setName("test4");
        model4.setPrice(10);
        skuDao.batchCreate(Arrays.asList(model, model4));
        assertEquals(3, skuDao.count());

        SkuModel test4 = skuDao.findByName("test4");
        assertEquals("test4", test4.getName());

        // test remove
        skuDao.remove(test4.getId());
        assertEquals(2, skuDao.count());
    }
}