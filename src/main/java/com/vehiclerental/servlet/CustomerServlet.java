package com.vehiclerental.servlet;

import com.vehiclerental.dao.CustomerDAO;
import com.vehiclerental.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servlet handling Customer CRUD operations, validation, and searching.
 */
@WebServlet(name = "CustomerServlet", urlPatterns = {"/customers", "/customers/*"})
public class CustomerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CustomerDAO customerDAO;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }

        consumeFlashMessages(request);

        switch (action) {
            case "add":
                showAddForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteCustomer(request, response);
                break;
            case "search":
                searchCustomers(request, response);
                break;
            case "list":
            default:
                listCustomers(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "insert":
                insertCustomer(request, response);
                break;
            case "update":
                updateCustomer(request, response);
                break;
            case "delete":
                deleteCustomer(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/customers");
                break;
        }
    }

    // --- Action Handlers ---

    private void listCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Customer> customerList = customerDAO.getAllCustomers();
        request.setAttribute("customerList", customerList);
        request.getRequestDispatcher("/customers/list.jsp").forward(request, response);
    }

    private void searchCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Customer> customerList;
        if (keyword != null && !keyword.trim().isEmpty()) {
            customerList = customerDAO.searchCustomer(keyword.trim());
            request.setAttribute("keyword", keyword.trim());
        } else {
            customerList = customerDAO.getAllCustomers();
        }
        request.setAttribute("customerList", customerList);
        request.getRequestDispatcher("/customers/list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/customers/add.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                Customer customer = customerDAO.getCustomerById(id);
                if (customer != null) {
                    request.setAttribute("customer", customer);
                    request.getRequestDispatcher("/customers/edit.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        setFlashMessage(request, "error", "Customer not found.");
        response.sendRedirect(request.getContextPath() + "/customers");
    }

    private void insertCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");

        StringBuilder errors = validateCustomerInput(name, phone, email, address, 0);

        if (errors.length() > 0) {
            request.setAttribute("errorMessage", errors.toString());
            request.setAttribute("name", name);
            request.setAttribute("phone", phone);
            request.setAttribute("email", email);
            request.setAttribute("address", address);
            request.getRequestDispatcher("/customers/add.jsp").forward(request, response);
            return;
        }

        Customer customer = new Customer(
                name.trim(),
                phone.trim(),
                email.trim().toLowerCase(),
                (address != null) ? address.trim() : ""
        );

        boolean success = customerDAO.addCustomer(customer);
        if (success) {
            setFlashMessage(request, "success", "Customer '" + customer.getName() + "' registered successfully!");
        } else {
            setFlashMessage(request, "error", "Failed to register customer. Please check database connection.");
        }
        response.sendRedirect(request.getContextPath() + "/customers");
    }

    private void updateCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String idStr = request.getParameter("customerId");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");

        int customerId;
        try {
            customerId = Integer.parseInt(idStr);
        } catch (Exception e) {
            setFlashMessage(request, "error", "Invalid customer ID.");
            response.sendRedirect(request.getContextPath() + "/customers");
            return;
        }

        StringBuilder errors = validateCustomerInput(name, phone, email, address, customerId);

        if (errors.length() > 0) {
            Customer customer = new Customer(customerId, name, phone, email, address);
            request.setAttribute("errorMessage", errors.toString());
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/customers/edit.jsp").forward(request, response);
            return;
        }

        Customer customer = new Customer(
                customerId,
                name.trim(),
                phone.trim(),
                email.trim().toLowerCase(),
                (address != null) ? address.trim() : ""
        );

        boolean success = customerDAO.updateCustomer(customer);
        if (success) {
            setFlashMessage(request, "success", "Customer details updated successfully!");
        } else {
            setFlashMessage(request, "error", "Failed to update customer details.");
        }
        response.sendRedirect(request.getContextPath() + "/customers");
    }

    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                if (customerDAO.hasActiveRentals(id)) {
                    setFlashMessage(request, "error", "Cannot delete customer: They currently have an ACTIVE vehicle rental.");
                } else {
                    boolean success = customerDAO.deleteCustomer(id);
                    if (success) {
                        setFlashMessage(request, "success", "Customer deleted successfully.");
                    } else {
                        setFlashMessage(request, "error", "Could not delete customer. Historical rental records exist for this customer.");
                    }
                }
            } catch (NumberFormatException e) {
                setFlashMessage(request, "error", "Invalid customer ID format.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/customers");
    }

    // --- Validation & Helper Methods ---

    private StringBuilder validateCustomerInput(String name, String phone, String email, String address, int excludeCustomerId) {
        StringBuilder errors = new StringBuilder();
        if (name == null || name.trim().isEmpty()) {
            errors.append("Customer Name is required. ");
        }
        if (phone == null || phone.trim().isEmpty()) {
            errors.append("Phone Number is required. ");
        } else if (!phone.matches("^[0-9+\\-\\s()]{7,20}$")) {
            errors.append("Phone Number format is invalid. ");
        }
        if (email == null || email.trim().isEmpty()) {
            errors.append("Email Address is required. ");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.append("Email Address format is invalid (e.g., user@example.com). ");
        } else if (customerDAO.isEmailExists(email.trim(), excludeCustomerId)) {
            errors.append("Email '").append(email.trim()).append("' is already registered to another customer. ");
        }
        return errors;
    }

    private void setFlashMessage(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        if ("success".equals(type)) {
            session.setAttribute("flashSuccess", message);
        } else {
            session.setAttribute("flashError", message);
        }
    }

    private void consumeFlashMessages(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object success = session.getAttribute("flashSuccess");
            if (success != null) {
                request.setAttribute("successMessage", success);
                session.removeAttribute("flashSuccess");
            }
            Object error = session.getAttribute("flashError");
            if (error != null) {
                request.setAttribute("errorMessage", error);
                session.removeAttribute("flashError");
            }
        }
    }
}
