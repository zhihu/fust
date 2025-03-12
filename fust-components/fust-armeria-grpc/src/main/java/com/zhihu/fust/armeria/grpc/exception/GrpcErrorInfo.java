package com.zhihu.fust.armeria.grpc.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhihu.fust.commons.exception.ExceptionUtils;
import com.zhihu.fust.commons.lang.StringUtils;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Business error entity that client-side can use
 */
public class GrpcErrorInfo implements GrpcBusinessError {
    private static final Logger log = LoggerFactory.getLogger(GrpcErrorInfo.class);
    public static final Metadata.Key<byte[]> GRPC_BIZ_ERROR_BIN_KEY = Metadata.Key.of("grpc-biz-error-bin", Metadata.BINARY_BYTE_MARSHALLER);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private String code;
    private String description;

    public GrpcErrorInfo() {
    }

    public GrpcErrorInfo(String code, String description) {
        this.code = code;
        this.description = description;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public static Optional<GrpcErrorInfo> fromJson(String value) {
        try {
            if (StringUtils.isEmpty(value)) {
                return Optional.empty();
            }
            return Optional.of(OBJECT_MAPPER.readValue(value, GrpcErrorInfo.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize business error info", e);
        }
        return Optional.empty();
    }

    public static String toJson(GrpcErrorInfo errorInfo) {
        try {
            return OBJECT_MAPPER.writeValueAsString(errorInfo);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize business error info", e);
        }
        return null;
    }

    /**
     * The client-side resolves the error message passed by the server-side
     *
     * @param e exception
     * @return Optional<GrpcErrorInfo>
     */
    public static Optional<GrpcErrorInfo> fromException(Exception e) {
        Status status = Status.fromThrowable(e);
        Exception grpcException = (Exception) ExceptionUtils.getRootCause(e);
        if (status.getCode() == Status.UNKNOWN.getCode() && grpcException instanceof StatusRuntimeException) {
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) grpcException;
            Metadata trailers = statusRuntimeException.getTrailers();
            return fromTrailers(trailers);
        }
        return Optional.empty();
    }

    public static Optional<GrpcErrorInfo> fromTrailers(Metadata trailers) {
        if (Objects.isNull(trailers)) {
            return Optional.empty();
        }

        String errorInfo = Optional.ofNullable(trailers.get(GRPC_BIZ_ERROR_BIN_KEY))
                .map(x -> new String(x, StandardCharsets.UTF_8))
                .orElse("");

        return GrpcErrorInfo.fromJson(errorInfo);
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", GrpcErrorInfo.class.getSimpleName() + "[", "]")
                .add("code='" + code + "'")
                .add("description='" + description + "'")
                .toString();
    }
}
