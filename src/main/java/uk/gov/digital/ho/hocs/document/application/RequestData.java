package uk.gov.digital.ho.hocs.document.application;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class RequestData implements HandlerInterceptor {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-Auth-UserId";
    public static final String USERNAME_HEADER = "X-Auth-Username";
    public static final String GROUP_HEADER = "X-Auth-Groups";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.clear();
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));
        MDC.put(USER_ID_HEADER, initialiseUserId(request));
        MDC.put(USERNAME_HEADER, initialiseUserName(request));
        MDC.put(GROUP_HEADER, initialiseGroups(request));

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setHeader(CORRELATION_ID_HEADER, correlationId());
        response.setHeader(USER_ID_HEADER, userId());
        response.setHeader(USERNAME_HEADER, username());
        response.setHeader(GROUP_HEADER, groups());
        MDC.clear();
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return StringUtils.hasText(correlationId) ? correlationId : UUID.randomUUID().toString();
    }

    private String initialiseUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return StringUtils.hasText(userId) ? userId : "";
    }

    private String initialiseUserName(HttpServletRequest request) {
        String username = request.getHeader(USERNAME_HEADER);
        return StringUtils.hasText(username) ? username : "";
    }

    private String initialiseGroups(HttpServletRequest request) {
        String groups = request.getHeader(GROUP_HEADER);
        return StringUtils.hasText(groups) ? groups : "";
    }

    public String correlationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String userId() {
        return MDC.get(USER_ID_HEADER);
    }

    public String username() {
        return MDC.get(USERNAME_HEADER);
    }

    public String groups() {
        return MDC.get(GROUP_HEADER);
    }

}
