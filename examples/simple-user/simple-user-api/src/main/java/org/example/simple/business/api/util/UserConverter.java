package org.example.simple.business.api.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import org.example.simple.business.api.dto.UserDto;
import org.example.simple.business.model.UserModel;

/**
 * 用户模型转换工具类
 */
public class UserConverter {
    
    /**
     * 将UserModel转换为UserDto
     */
    public static UserDto toDto(UserModel model) {
        if (model == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(model, dto);
        return dto;
    }
    
    /**
     * 将UserDto转换为UserModel
     */
    public static UserModel toModel(UserDto dto) {
        if (dto == null) {
            return null;
        }
        
        UserModel model = new UserModel();
        BeanUtils.copyProperties(dto, model);
        return model;
    }
    
    /**
     * 将UserModel列表转换为UserDto列表
     */
    public static List<UserDto> toDtoList(List<UserModel> models) {
        if (models == null) {
            return List.of();
        }
        
        return models.stream()
                .map(UserConverter::toDto)
                .collect(Collectors.toList());
    }
} 