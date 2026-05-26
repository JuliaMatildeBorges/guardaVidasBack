package com.example.demo.config;

import com.example.demo.annotations.Admin;
import com.example.demo.annotations.Public;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;
import java.util.List;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // @Public: libera sem autenticação
        if (hasAnnotation(handlerMethod, Public.class)) {
            return true;
        }

        // Valida o token JWT do header
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token ausente ou inválido.");
            return false;
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        List<SimpleGrantedAuthority> authorities = role != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                : List.of();

        // Popula o SecurityContext para uso nos controllers
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.getContextHolderStrategy().setDeferredContext(() -> context);

        // @Admin: exige role ADMIN
        if (hasAnnotation(handlerMethod, Admin.class)) {
            boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Acesso negado para este usuário.");
                return false;
            }
        }

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean hasAnnotation(HandlerMethod handler, Class<? extends Annotation> annotation) {
        return AnnotatedElementUtils.hasAnnotation(handler.getMethod(), annotation)
                || AnnotatedElementUtils.hasAnnotation(handler.getBeanType(), annotation);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
