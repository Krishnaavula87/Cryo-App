package com.cryo.export.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleRateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, UserRate> users =
            new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SECONDS = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String user =
                request.getUserPrincipal() != null
                        ? request.getUserPrincipal().getName()
                        : "anonymous";

        UserRate rate =
                users.computeIfAbsent(user, k -> new UserRate());

        synchronized (rate) {

            long now = Instant.now().getEpochSecond();

            if (now - rate.windowStart > WINDOW_SECONDS) {
                rate.windowStart = now;
                rate.count = 0;
            }

            if (rate.count >= MAX_REQUESTS) {
                response.setStatus(429);
                response.getWriter().write(
                        "Too many export requests. Try later.");
                return;
            }

            rate.count++;
        }

        filterChain.doFilter(request, response);
    }

    private static class UserRate {
        long windowStart = Instant.now().getEpochSecond();
        int count = 0;
    }
}