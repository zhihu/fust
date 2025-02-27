package com.zhihu.fust.example.business.model;

import com.zhihu.fust.spring.mybatis.annotations.DbAutoColumn;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Table(name = "yd_user")
@Getter
@Setter
public class UserModel {
    @Id
    private long id;
    private LocalDate birthday;
    private String name;

    @DbAutoColumn
    private Instant createdAt;
    @DbAutoColumn
    private Instant updatedAt;
}