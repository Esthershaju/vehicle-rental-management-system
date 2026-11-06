package com.vehiclerental.servlet;

import com.vehiclerental.dao.VehicleDAO;
import com.vehiclerental.model.Vehicle;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Servlet handling Vehicle CRUD operations, searching, and filtering.
 */
@WebServlet(name = "VehicleServlet", urlPatterns = {"/vehicles", "/vehicles/*"})
public class VehicleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private VehicleDAO vehicleDAO;

    @Override
    public void init() throws ServletException {
        vehicleDAO = new VehicleDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }

        // Transfer flash messages from session to request if present
        consumeFlashMessages(request);

        switch (action) {
            case "add":
                showAddForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteVehicle(request, response);
                break;
            case "search":
                searchVehicles(request, response);
                break;
            case "available":
                listAvailableVehicles(request, response);
                break;
            case "list":
            default:
                listVehicles(request, response);
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
                insertVehicle(request, response);
                break;
            case "update":
                updateVehicle(request, response);
                break;
            case "delete":
                deleteVehicle(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/vehicles");
                break;
        }
    }

    // --- Action Handlers ---

    private void listVehicles(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Vehicle> vehicleList = vehicleDAO.getAllVehicles();
        request.setAttribute("vehicleList", vehicleList);
        request.setAttribute("activeFilter", "all");
        request.getRequestDispatcher("/vehicles/list.jsp").forward(request, response);
    }

    private void listAvailableVehicles(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Vehicle> vehicleList = vehicleDAO.getAvailableVehicles();
        request.setAttribute("vehicleList", vehicleList);
        request.setAttribute("activeFilter", "available");
        request.getRequestDispatcher("/vehicles/list.jsp").forward(request, response);
    }

    private void searchVehicles(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Vehicle> vehicleList;
        if (keyword != null && !keyword.trim().isEmpty()) {
            vehicleList = vehicleDAO.searchVehicles(keyword.trim());
            request.setAttribute("keyword", keyword.trim());
        } else {
            vehicleList = vehicleDAO.getAllVehicles();
        }
        request.setAttribute("vehicleList", vehicleList);
        request.getRequestDispatcher("/vehicles/list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/vehicles/add.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                Vehicle existingVehicle = vehicleDAO.getVehicleById(id);
                if (existingVehicle != null) {
                    request.setAttribute("vehicle", existingVehicle);
                    request.getRequestDispatcher("/vehicles/edit.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        setFlashMessage(request, "error", "Vehicle not found.");
        response.sendRedirect(request.getContextPath() + "/vehicles");
    }

    private void insertVehicle(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String vehicleNumber = request.getParameter("vehicleNumber");
        String vehicleName = request.getParameter("vehicleName");
        String vehicleType = request.getParameter("vehicleType");
        String brand = request.getParameter("brand");
        String model = request.getParameter("model");
        String priceStr = request.getParameter("rentalPrice");
        String status = request.getParameter("availabilityStatus");

        // Validation
        StringBuilder errors = new StringBuilder();
        if (isEmpty(vehicleNumber)) errors.append("Vehicle Number is required. ");
        if (isEmpty(vehicleName)) errors.append("Vehicle Name is required. ");
        if (isEmpty(vehicleType)) errors.append("Vehicle Type is required. ");
        
        double price = 0.0;
        if (isEmpty(priceStr)) {
            errors.append("Rental Price is required. ");
        } else {
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    errors.append("Rental Price must be greater than zero. ");
                }
            } catch (NumberFormatException e) {
                errors.append("Rental Price must be a valid number. ");
            }
        }

        if (vehicleDAO.isVehicleNumberExists(vehicleNumber, 0)) {
            errors.append("A vehicle with number '").append(vehicleNumber).append("' already exists. ");
        }

        if (errors.length() > 0) {
            request.setAttribute("errorMessage", errors.toString());
            request.setAttribute("vehicleNumber", vehicleNumber);
            request.setAttribute("vehicleName", vehicleName);
            request.setAttribute("vehicleType", vehicleType);
            request.setAttribute("brand", brand);
            request.setAttribute("model", model);
            request.setAttribute("rentalPrice", priceStr);
            request.setAttribute("availabilityStatus", status);
            request.getRequestDispatcher("/vehicles/add.jsp").forward(request, response);
            return;
        }

        Vehicle vehicle = new Vehicle(
                vehicleNumber.trim().toUpperCase(),
                vehicleName.trim(),
                vehicleType.trim(),
                (brand != null) ? brand.trim() : "",
                (model != null) ? model.trim() : "",
                price,
                (status != null && !status.trim().isEmpty()) ? status.trim() : "AVAILABLE"
        );

        boolean success = vehicleDAO.addVehicle(vehicle);
        if (success) {
            setFlashMessage(request, "success", "Vehicle '" + vehicle.getVehicleName() + "' added successfully!");
        } else {
            setFlashMessage(request, "error", "Failed to add vehicle. Please check database connection.");
        }
        response.sendRedirect(request.getContextPath() + "/vehicles");
    }

    private void updateVehicle(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String idStr = request.getParameter("vehicleId");
        String vehicleNumber = request.getParameter("vehicleNumber");
        String vehicleName = request.getParameter("vehicleName");
        String vehicleType = request.getParameter("vehicleType");
        String brand = request.getParameter("brand");
        String model = request.getParameter("model");
        String priceStr = request.getParameter("rentalPrice");
        String status = request.getParameter("availabilityStatus");

        int vehicleId;
        try {
            vehicleId = Integer.parseInt(idStr);
        } catch (Exception e) {
            setFlashMessage(request, "error", "Invalid vehicle ID.");
            response.sendRedirect(request.getContextPath() + "/vehicles");
            return;
        }

        // Validation
        StringBuilder errors = new StringBuilder();
        if (isEmpty(vehicleNumber)) errors.append("Vehicle Number is required. ");
        if (isEmpty(vehicleName)) errors.append("Vehicle Name is required. ");
        if (isEmpty(vehicleType)) errors.append("Vehicle Type is required. ");

        double price = 0.0;
        if (isEmpty(priceStr)) {
            errors.append("Rental Price is required. ");
        } else {
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    errors.append("Rental Price must be greater than zero. ");
                }
            } catch (NumberFormatException e) {
                errors.append("Rental Price must be a valid number. ");
            }
        }

        if (vehicleDAO.isVehicleNumberExists(vehicleNumber, vehicleId)) {
            errors.append("A vehicle with number '").append(vehicleNumber).append("' already exists. ");
        }

        if (errors.length() > 0) {
            Vehicle vehicle = new Vehicle(vehicleId, vehicleNumber, vehicleName, vehicleType, brand, model, price, status);
            request.setAttribute("errorMessage", errors.toString());
            request.setAttribute("vehicle", vehicle);
            request.getRequestDispatcher("/vehicles/edit.jsp").forward(request, response);
            return;
        }

        Vehicle vehicle = new Vehicle(
                vehicleId,
                vehicleNumber.trim().toUpperCase(),
                vehicleName.trim(),
                vehicleType.trim(),
                (brand != null) ? brand.trim() : "",
                (model != null) ? model.trim() : "",
                price,
                status
        );

        boolean success = vehicleDAO.updateVehicle(vehicle);
        if (success) {
            setFlashMessage(request, "success", "Vehicle updated successfully!");
        } else {
            setFlashMessage(request, "error", "Failed to update vehicle.");
        }
        response.sendRedirect(request.getContextPath() + "/vehicles");
    }

    private void deleteVehicle(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                if (vehicleDAO.hasActiveRentals(id)) {
                    setFlashMessage(request, "error", "Cannot delete vehicle: It is currently assigned to an ACTIVE rental.");
                } else {
                    boolean success = vehicleDAO.deleteVehicle(id);
                    if (success) {
                        setFlashMessage(request, "success", "Vehicle deleted successfully.");
                    } else {
                        setFlashMessage(request, "error", "Could not delete vehicle. It may be referenced by historical rental records.");
                    }
                }
            } catch (NumberFormatException e) {
                setFlashMessage(request, "error", "Invalid vehicle ID format.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/vehicles");
    }

    // --- Helper Methods ---

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
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
