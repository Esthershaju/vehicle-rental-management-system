package com.vehiclerental.servlet;

import com.vehiclerental.dao.CustomerDAO;
import com.vehiclerental.dao.RentalDAO;
import com.vehiclerental.dao.VehicleDAO;
import com.vehiclerental.model.Rental;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet for populating and displaying the administrator dashboard.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private RentalDAO rentalDAO;

    @Override
    public void init() throws ServletException {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        rentalDAO = new RentalDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Retrieve aggregate counts for KPI metric cards
            int totalVehicles = vehicleDAO.getTotalVehiclesCount();
            int availableVehicles = vehicleDAO.getAvailableVehiclesCount();
            int rentedVehicles = vehicleDAO.getRentedVehiclesCount();
            int totalCustomers = customerDAO.getTotalCustomersCount();
            int totalRentals = rentalDAO.getTotalRentalsCount();
            int activeRentals = rentalDAO.getActiveRentalsCount();

            // Retrieve recent 5 rentals for the activity feed
            List<Rental> recentRentals = rentalDAO.getRecentRentals(5);

            // Set attributes for dashboard.jsp
            request.setAttribute("totalVehicles", totalVehicles);
            request.setAttribute("availableVehicles", availableVehicles);
            request.setAttribute("rentedVehicles", rentedVehicles);
            request.setAttribute("totalCustomers", totalCustomers);
            request.setAttribute("totalRentals", totalRentals);
            request.setAttribute("activeRentals", activeRentals);
            request.setAttribute("recentRentals", recentRentals);

        } catch (Exception e) {
            System.err.println("[DashboardServlet] Error loading dashboard metrics: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Unable to load complete dashboard metrics. Please check database connection.");
        }

        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}
