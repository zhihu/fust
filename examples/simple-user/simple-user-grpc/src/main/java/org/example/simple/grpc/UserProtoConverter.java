package org.example.simple.grpc;

import com.google.protobuf.Timestamp;
import org.example.simple.business.model.UserModel;
import org.example.simple.user.proto.CreateUserRequest;
import org.example.simple.user.proto.UpdateUserRequest;
import org.example.simple.user.proto.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Proto转换工具类
 */
public class UserProtoConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 将UserModel转换为Proto的User
     */
    public static User toProto(UserModel model) {
        if (model == null) {
            return User.getDefaultInstance();
        }

        User.Builder builder = User.newBuilder()
                .setId(model.getId())
                .setName(model.getName())
                .setBirthday(model.getBirthday().format(DATE_FORMATTER));

        // 处理创建和更新时间
        if (model.getCreatedAt() != null) {
            Instant instant = model.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant();
            builder.setCreatedAt(Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build());
        }

        if (model.getUpdatedAt() != null) {
            Instant instant = model.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant();
            builder.setUpdatedAt(Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build());
        }

        return builder.build();
    }

    /**
     * 将CreateUserRequest转换为UserModel
     */
    public static UserModel toModel(CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        UserModel model = new UserModel();
        model.setName(request.getName());
        model.setBirthday(LocalDate.parse(request.getBirthday(), DATE_FORMATTER));
        return model;
    }

    /**
     * 将UpdateUserRequest转换为UserModel
     */
    public static UserModel toModel(UpdateUserRequest request) {
        if (request == null) {
            return null;
        }

        UserModel model = new UserModel();
        model.setId(request.getId());
        model.setName(request.getName());
        model.setBirthday(LocalDate.parse(request.getBirthday(), DATE_FORMATTER));
        return model;
    }

    /**
     * 将UserModel列表转换为Proto的User列表
     */
    public static List<User> toProtoList(List<UserModel> models) {
        if (models == null) {
            return List.of();
        }

        return models.stream()
                .map(UserProtoConverter::toProto)
                .collect(Collectors.toList());
    }
}
