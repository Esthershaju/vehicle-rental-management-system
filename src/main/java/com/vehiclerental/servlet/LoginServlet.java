package com.vehiclerental.servlet;

import com.vehiclerental.dao.CustomerDAO;
import com.vehiclerental.dao.UserDAO;
import com.vehiclerental.model.Customer;
import com.vehiclerental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet handling dual-role login (Administrator vs Customer) and session establishment.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private CustomerDAO customerDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        customerDAO = new CustomerDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // If already logged in, redirect to respective role dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if (user.isCustomer()) {
                response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            }
            return;
        }

        // Handle error messages from AuthFilter or logout
        String error = request.getParameter("error");
        if ("auth_required".equals(error)) {
            request.setAttribute("errorMessage", "Please log in to access that page.");
        } else if ("unauthorized".equals(error)) {
            request.setAttribute("errorMessage", "Access Denied: You do not have permission to view that page.");
        }

        String msg = request.getParameter("msg");
        if ("logged_out".equals(msg)) {
            request.setAttribute("successMessage", "You have been logged out successfully.");
        } else if ("registered".equals(msg)) {
            request.setAttribute("successMessage", "Account registered successfully! Please log in below.");
            request.setAttribute("activeTab", "customer");
        }

        String roleParam = request.getParameter("role");
        if (roleParam != null) {
            String requestedRole = roleParam.trim().toLowerCase();
            if ("admin".equals(requestedRole) || "customer".equals(requestedRole) || "register".equals(requestedRole)) {
                request.setAttribute("activeTab", requestedRole);
            }
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String role = request.getParameter("role");
        if (role == null || role.trim().isEmpty()) {
            role = "admin";
        }
        role = role.trim().toLowerCase();
        if (!"admin".equals(role) && !"customer".equals(role)) {
            request.setAttribute("errorMessage", "Please select a valid account type.");
            request.setAttribute("activeTab", "admin");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Basic server-side validation
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Both username/email and password are required.");
            request.setAttribute("enteredUsername", username);
            request.setAttribute("activeTab", role);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        if ("customer".equals(role)) {
            // Customer Authentication
            Customer customer = customerDAO.authenticate(username.trim(), password);
            if (customer != null) {
                if (request.getSession(false) != null) {
                    request.changeSessionId();
                }
                HttpSession session = request.getSession(true);
                User user = new User(customer.getCustomerId(), customer.getEmail(), null, "CUSTOMER", customer.getCustomerId(), customer.getName());
                session.setAttribute("user", user);
                session.setAttribute("role", "CUSTOMER");
                session.setAttribute("username", customer.getEmail());
                session.setAttribute("displayName", customer.getName());
                response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            } else {
                request.setAttribute("errorMessage", "Invalid customer email or password. Please try again.");
                request.setAttribute("enteredUsername", username);
                request.setAttribute("activeTab", "customer");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } else {
            // Administrator Authentication
            User user = userDAO.login(username.trim(), password);
            if (user != null) {
                if (request.getSession(false) != null) {
                    request.changeSessionId();
                }
                HttpSession session = request.getSession(true);
                user.setPassword(null);
                user.setRole("ADMIN");
                user.setFullName("Administrator");
                session.setAttribute("user", user);
                session.setAttribute("role", "ADMIN");
                session.setAttribute("username", user.getUsername());
                session.setAttribute("displayName", "Administrator");
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                request.setAttribute("errorMessage", "Invalid administrator credentials. Please try again.");
                request.setAttribute("enteredUsername", username);
                request.setAttribute("activeTab", "admin");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        }
    }
}
