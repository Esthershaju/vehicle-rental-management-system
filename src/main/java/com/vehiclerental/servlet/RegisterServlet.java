package com.vehiclerental.servlet;

import com.vehiclerental.dao.CustomerDAO;
import com.vehiclerental.model.Customer;
import com.vehiclerental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Servlet handling customer self-registration.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private CustomerDAO customerDAO;

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("activeTab", "register");
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String address = request.getParameter("address");

        // Validate mandatory fields
        if (name == null || name.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            
            request.setAttribute("errorMessage", "All fields marked with * are required.");
            request.setAttribute("activeTab", "register");
            request.setAttribute("regName", name);
            request.setAttribute("regPhone", phone);
            request.setAttribute("regEmail", email);
            request.setAttribute("regAddress", address);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        if (name.trim().length() > 100 || phone.trim().length() > 15 ||
            email.trim().length() > 100 || (address != null && address.trim().length() > 255) ||
            password.length() > 255) {
            request.setAttribute("errorMessage", "One or more fields exceed the maximum allowed length.");
            request.setAttribute("activeTab", "register");
            request.setAttribute("regName", name);
            request.setAttribute("regPhone", phone);
            request.setAttribute("regEmail", email);
            request.setAttribute("regAddress", address);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // Email format check
        String cleanEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            request.setAttribute("errorMessage", "Please provide a valid email address.");
            request.setAttribute("activeTab", "register");
            request.setAttribute("regName", name);
            request.setAttribute("regPhone", phone);
            request.setAttribute("regEmail", email);
            request.setAttribute("regAddress", address);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("errorMessage", "Password must be at least 6 characters long.");
            request.setAttribute("activeTab", "register");
            request.setAttribute("regName", name);
            request.setAttribute("regPhone", phone);
            request.setAttribute("regEmail", email);
            request.setAttribute("regAddress", address);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // Check duplicate email
        if (customerDAO.isEmailExists(cleanEmail, 0)) {
            request.setAttribute("errorMessage", "An account with this email already exists. Please log in.");
            request.setAttribute("activeTab", "customer");
            request.setAttribute("enteredUsername", cleanEmail);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // Create new customer
        Customer newCustomer = new Customer(name.trim(), phone.trim(), cleanEmail, address != null ? address.trim() : "", password);
        boolean success = customerDAO.addCustomer(newCustomer);

        if (success) {
            // Auto login
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }
            HttpSession session = request.getSession(true);
            User user = new User(newCustomer.getCustomerId(), newCustomer.getEmail(), null, "CUSTOMER", newCustomer.getCustomerId(), newCustomer.getName());
            session.setAttribute("user", user);
            session.setAttribute("role", "CUSTOMER");
            session.setAttribute("username", newCustomer.getEmail());
            session.setAttribute("displayName", newCustomer.getName());
            response.sendRedirect(request.getContextPath() + "/customer/dashboard?msg=welcome");
        } else {
            request.setAttribute("errorMessage", "Failed to create account due to a database error. Please try again.");
            request.setAttribute("activeTab", "register");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
