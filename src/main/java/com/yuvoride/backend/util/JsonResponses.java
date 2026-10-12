package com.yuvoride.backend.util;

import com.yuvoride.backend.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

public final class JsonResponses {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private JsonResponses() {
    }

    public static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getOutputStream(), ApiResponse.error(message));
    }
}
