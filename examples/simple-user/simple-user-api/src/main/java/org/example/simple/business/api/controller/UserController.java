package org.example.simple.business.api.controller;

import org.example.simple.business.api.dto.UserDto;
import org.example.simple.business.api.response.ApiResponse;
import org.example.simple.business.api.util.UserConverter;
import org.example.simple.business.exception.UserNotFoundException;
import org.example.simple.business.model.UserModel;
import org.example.simple.business.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        UserModel user = userService.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return ApiResponse.success(UserConverter.toDto(user));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDto> createUser(@RequestBody UserDto userDto) {
        UserModel userModel = UserConverter.toModel(userDto);
        boolean created = userService.createUser(userModel);
        if (created) {
            return ApiResponse.success(UserConverter.toDto(userModel));
        } else {
            return ApiResponse.error(500, "Failed to create user");
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto userDto) {
        // 确保用户存在
        UserModel existingUser = userService.getUserById(id);
        if (existingUser == null) {
            throw new UserNotFoundException(id);
        }

        UserModel userModel = UserConverter.toModel(userDto);
        userModel.setId(id);
        boolean updated = userService.updateUser(userModel);
        if (updated) {
            return ApiResponse.success(UserConverter.toDto(userModel));
        } else {
            return ApiResponse.error(500, "Failed to update user");
        }
    }

    @PatchMapping("/{id}")
    public ApiResponse<UserDto> patchUser(
            @PathVariable Long id,
            @RequestBody UserDto userDto) {
        // 确保用户存在
        UserModel existingUser = userService.getUserById(id);
        if (existingUser == null) {
            throw new UserNotFoundException(id);
        }

        UserModel userModel = UserConverter.toModel(userDto);
        userModel.setId(id);
        boolean patched = userService.patchUser(userModel);
        if (patched) {
            return ApiResponse.success(UserConverter.toDto(userModel));
        } else {
            return ApiResponse.error(500, "Failed to patch user");
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long id) {
        // 确保用户存在
        UserModel existingUser = userService.getUserById(id);
        if (existingUser == null) {
            throw new UserNotFoundException(id);
        }

        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(500, "Failed to delete user");
        }
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<UserDto>> batchCreateUsers(
            @RequestBody List<UserDto> userDtos) {
        List<UserModel> userModels = userDtos.stream()
                .map(UserConverter::toModel)
                .toList();

        boolean created = userService.batchCreateUsers(userModels);
        if (created) {
            return ApiResponse.success(UserConverter.toDtoList(userModels));
        } else {
            return ApiResponse.error(500, "Failed to batch create users");
        }
    }

    @DeleteMapping("/batch")
    public ApiResponse<Integer> batchDeleteUsers(
            @RequestBody List<Long> ids) {
        int deleted = userService.batchDeleteUsers(ids);
        return ApiResponse.success(deleted);
    }

    @GetMapping("/cache/{id}")
    public ApiResponse<UserDto> getUserFromCache(
            @PathVariable Long id) {
        Optional<UserModel> optionalUser = userService.findByCache(id);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(id);
        }
        return ApiResponse.success(UserConverter.toDto(optionalUser.get()));
    }
} 