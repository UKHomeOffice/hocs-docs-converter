package uk.gov.digital.ho.hocs.document.application;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
public class RequestData implements HandlerInterceptor {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-Auth-UserId";
    public static final String USERNAME_HEADER = "X-Auth-Username";
    public static final String GROUP_HEADER = "X-Auth-Groups";

    public static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();

    private static final String ANONYMOUS = "anonymous";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        THREAD_LOCAL.set(
            Map.of(
            CORRELATION_ID_HEADER, initialiseCorrelationId(request),
            USER_ID_HEADER, initialiseUserId(request),
            USERNAME_HEADER, initialiseUserName(request),
            GROUP_HEADER, initialiseGroups(request)
            )
        );

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        THREAD_LOCAL.remove();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setHeader(CORRELATION_ID_HEADER, correlationId());
        response.setHeader(USER_ID_HEADER, userId());
        response.setHeader(USERNAME_HEADER, username());
        response.setHeader(GROUP_HEADER, groups());
        THREAD_LOCAL.remove();
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return StringUtils.hasText(correlationId) ? correlationId : UUID.randomUUID().toString();
    }

    private String initialiseUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return StringUtils.hasText(userId) ? userId : ANONYMOUS;
    }

    private String initialiseUserName(HttpServletRequest request) {
        String username = request.getHeader(USERNAME_HEADER);
        return StringUtils.hasText(username) ? username : ANONYMOUS;
    }

    private String initialiseGroups(HttpServletRequest request) {
        String groups = request.getHeader(GROUP_HEADER);
        return StringUtils.hasText(groups) ? groups : "/QU5PTllNT1VTCg==";
    }

    public String correlationId() {
        Map<String, String> threadMap = THREAD_LOCAL.get();
        return threadMap.get(CORRELATION_ID_HEADER);
    }

    public String userId() {
        Map<String, String> threadMap = THREAD_LOCAL.get();
        return threadMap.get(USER_ID_HEADER);
    }

    public String username() {
        Map<String, String> threadMap = THREAD_LOCAL.get();
        return threadMap.get(USERNAME_HEADER);
    }

    public String groups() {
        Map<String, String> threadMap = THREAD_LOCAL.get();
        return threadMap.get(GROUP_HEADER);
    }

}
