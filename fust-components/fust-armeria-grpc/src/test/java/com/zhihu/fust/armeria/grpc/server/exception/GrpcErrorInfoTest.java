package com.zhihu.fust.armeria.grpc.server.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhihu.fust.armeria.grpc.exception.GrpcErrorInfo;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GrpcErrorInfoTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void testFromValidString() throws JsonProcessingException {
        String json = "{\"code\":\"ERROR_CODE\",\"description\":\"Error description\"}";
        GrpcErrorInfo expected = new GrpcErrorInfo("ERROR_CODE", "Error description");

        Optional<GrpcErrorInfo> result = GrpcErrorInfo.fromJson(json);
        assertTrue(result.isPresent());
        assertEquals(expected.getCode(), result.get().getCode());
        assertEquals(expected.getDescription(), result.get().getDescription());
    }

    @Test
    void testFromEmptyString() {
        Optional<GrpcErrorInfo> result = GrpcErrorInfo.fromJson("");
        assertFalse(result.isPresent());
    }

    @Test
    void testFromInvalidString() {
        String invalidJson = "invalid json";
        Optional<GrpcErrorInfo> result = GrpcErrorInfo.fromJson(invalidJson);
        assertFalse(result.isPresent());
    }

    @Test
    void testFromExceptionWithGrpcError() {
        Metadata metadata = new Metadata();
        metadata.put(GrpcErrorInfo.GRPC_BIZ_ERROR_BIN_KEY, "{\"code\":\"ERROR_CODE\",\"description\":\"❎错误ERROR\"}".getBytes(StandardCharsets.UTF_8));
        StatusRuntimeException exception = new StatusRuntimeException(Status.UNKNOWN, metadata);

        Optional<GrpcErrorInfo> result = GrpcErrorInfo.fromException(exception);
        assertTrue(result.isPresent());
        assertEquals("ERROR_CODE", result.get().getCode());
        assertEquals("❎错误ERROR", result.get().getDescription());
    }

    @Test
    void testFromExceptionWithoutGrpcError() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.UNKNOWN);

        Optional<GrpcErrorInfo> result = GrpcErrorInfo.fromException(exception);
        assertFalse(result.isPresent());
    }

    @Test
    void testToJson() throws JsonProcessingException {
        GrpcErrorInfo errorInfo = new GrpcErrorInfo("ERROR_CODE", "Error description");
        String expectedJson = OBJECT_MAPPER.writeValueAsString(errorInfo);

        String result = GrpcErrorInfo.toJson(errorInfo);
        assertEquals(expectedJson, result);
    }
}