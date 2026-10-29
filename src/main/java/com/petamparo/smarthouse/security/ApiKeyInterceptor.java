package com.petamparo.smarthouse.security;

import com.petamparo.smarthouse.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    public static final String DEVICE_KEY_HEADER = "X-Device-Key";
    public static final String ADMIN_KEY_HEADER = "X-Admin-Key";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyInterceptor.class);
    private static final String READINGS_PATH = "/api/readings";

    private final byte[] deviceKey;
    private final byte[] adminKey;

    public ApiKeyInterceptor(@Value("${smarthouse.security.device-api-key:}") String deviceKey,
                             @Value("${smarthouse.security.admin-api-key:}") String adminKey) {
        this.deviceKey = deviceKey.strip().getBytes(StandardCharsets.UTF_8);
        this.adminKey = adminKey.strip().getBytes(StandardCharsets.UTF_8);

        if (deviceKey.isBlank() || adminKey.isBlank()) {
            log.warn("DEVICE_API_KEY ou ADMIN_API_KEY não configurada; as escritas correspondentes serão recusadas");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method) || HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        boolean deviceEndpoint = HttpMethod.POST.matches(method) && READINGS_PATH.equals(request.getRequestURI());
        String header = deviceEndpoint ? DEVICE_KEY_HEADER : ADMIN_KEY_HEADER;
        byte[] expected = deviceEndpoint ? deviceKey : adminKey;

        if (!matches(request.getHeader(header), expected)) {
            throw new UnauthorizedException("Header %s ausente ou inválido".formatted(header));
        }
        return true;
    }

    private static boolean matches(String provided, byte[] expected) {
        if (expected.length == 0 || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(provided.getBytes(StandardCharsets.UTF_8), expected);
    }
}
