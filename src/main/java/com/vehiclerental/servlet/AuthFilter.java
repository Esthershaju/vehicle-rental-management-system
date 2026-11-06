package com.vehiclerental.servlet;

import com.vehiclerental.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filter to enforce authentication and Role-Based Access Control (RBAC).
 * Separates Administrator routes from Customer self-service routes.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/dashboard", "/dashboard.html", "/dashboard.jsp", "/vehicles/*", "/customers/*", "/rentals/*", "/customer/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Prevent browser caching of protected views
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        boolean isLoggedIn = (user != null);

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String relativePath = uri.substring(contextPath.length());

        if (isLoggedIn) {
            // Check role permissions
            if (relativePath.startsWith("/customer")) {
                // Customer or Admin can access customer endpoints
                if ("/customer/dashboard.html".equals(relativePath) || "/customer/dashboard.jsp".equals(relativePath)) {
                    res.sendRedirect(contextPath + "/customer/dashboard");
                    return;
                }
                chain.doFilter(request, response);
            } else {
                // Admin management routes (/dashboard, /vehicles, /customers, /rentals)
                if (user.isAdmin()) {
                    if ("/dashboard.html".equals(relativePath) || "/dashboard.jsp".equals(relativePath)) {
                        res.sendRedirect(contextPath + "/dashboard");
                        return;
                    }
                    chain.doFilter(request, response);
                } else {
                    // Logged in as customer, but trying to access admin pages
                    res.sendRedirect(contextPath + "/customer/dashboard?error=unauthorized");
                }
            }
        } else {
            // Unauthenticated: redirect to login page with appropriate role tab hint
            if (relativePath.startsWith("/customer")) {
                res.sendRedirect(contextPath + "/login?error=auth_required&role=customer");
            } else {
                res.sendRedirect(contextPath + "/login?error=auth_required&role=admin");
            }
        }
    }

    @Override
    public void destroy() {
    }
}
