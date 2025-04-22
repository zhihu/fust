package org.example.simple.business.model;

import com.zhihu.fust.spring.mybatis.annotations.DbAutoColumn;
import org.example.simple.business.dao.UserDao;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table(name = UserDao.TABLE_NAME, schema = "db1")
public class UserModel {
    @Id
    private Long id;
    @DbAutoColumn
    private LocalDateTime createdAt;
    @DbAutoColumn
    private LocalDateTime updatedAt;
    private LocalDate birthday;
    private String name;
} 