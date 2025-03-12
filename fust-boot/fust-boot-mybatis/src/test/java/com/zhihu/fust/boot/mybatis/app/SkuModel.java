package com.zhihu.fust.boot.mybatis.app;

import com.zhihu.fust.spring.mybatis.annotations.DbAutoColumn;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * @author hekaiqiang
 * @since 2021/9/1 16:11
 */
@Table(name = SkuModel.TABLE_NAME, schema = "sku")
public class SkuModel {

    public static final String TABLE_NAME = "table_sku";

    @Id
    private Long id;

    private String name;

    private Integer price;

    @DbAutoColumn
    private LocalDateTime createdAt;

    @DbAutoColumn
    private LocalDateTime updatedAt;

    public SkuModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
