package org.example.simple.grpc;

import org.example.simple.business.exception.UserNotFoundException;
import org.example.simple.business.model.UserModel;
import org.example.simple.business.service.UserService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.simple.user.proto.*;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeParseException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserHandler extends UserServiceGrpc.UserServiceImplBase {
    private final UserService userService;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        try {
            log.info("Creating user: {}", request.getName());

            UserModel userModel = UserProtoConverter.toModel(request);
            boolean created = userService.createUser(userModel);

            if (created) {
                User response = UserProtoConverter.toProto(userModel);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                        .withDescription("Failed to create user")));
            }
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", request.getBirthday());
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("Invalid date format. Expected yyyy-MM-dd but got: " + request.getBirthday())));
        } catch (Exception e) {
            log.error("Error creating user", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        try {
            long userId = request.getId();
            log.info("Getting user by ID: {}", userId);

            UserModel userModel = userService.getUserById(userId);
            User response = UserProtoConverter.toProto(userModel);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", request.getId());
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND
                    .withDescription(e.getMessage())));
        } catch (Exception e) {
            log.error("Error getting user", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<User> responseObserver) {
        try {
            long userId = request.getId();
            log.info("Updating user: {}", userId);

            // 确保用户存在
            userService.getUserById(userId);

            UserModel userModel = UserProtoConverter.toModel(request);
            boolean updated = userService.updateUser(userModel);

            if (updated) {
                User response = UserProtoConverter.toProto(userModel);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                        .withDescription("Failed to update user")));
            }
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", request.getId());
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND
                    .withDescription(e.getMessage())));
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", request.getBirthday());
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("Invalid date format. Expected yyyy-MM-dd but got: " + request.getBirthday())));
        } catch (Exception e) {
            log.error("Error updating user", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        try {
            long userId = request.getId();
            log.info("Deleting user: {}", userId);

            // 确保用户存在
            userService.getUserById(userId);

            boolean deleted = userService.deleteUser(userId);
            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                    .setSuccess(deleted)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", request.getId());
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND
                    .withDescription(e.getMessage())));
        } catch (Exception e) {
            log.error("Error deleting user", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }
}
