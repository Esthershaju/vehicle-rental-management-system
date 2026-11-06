package com.vehiclerental.servlet;

import com.vehiclerental.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Simple JSON API for user session data.
 * This satisfies the requirement of representing user information in a JSON structure.
 */
@WebServlet(name = "UserApiServlet", urlPatterns = {"/api/user"})
public class UserApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        StringBuilder json = new StringBuilder();
        json.append("{");

        if (user != null) {
            json.append("\"authenticated\":true,");
            json.append("\"userId\":").append(user.getUserId()).append(",");
            json.append("\"username\":\"").append(escape(user.getUsername())).append("\",");
            json.append("\"role\":\"").append(escape(user.getRole())).append("\",");
            json.append("\"fullName\":\"").append(escape(user.getFullName() == null ? user.getUsername() : user.getFullName())).append("\",");
            json.append("\"customerId\":").append(user.getCustomerId() == null ? "null" : user.getCustomerId());
        } else {
            json.append("\"authenticated\":false,");
            json.append("\"userId\":null,");
            json.append("\"username\":\"\",");
            json.append("\"role\":\"GUEST\",");
            json.append("\"fullName\":\"\",");
            json.append("\"customerId\":null");
        }

        json.append("}");
        response.getWriter().write(json.toString());
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
