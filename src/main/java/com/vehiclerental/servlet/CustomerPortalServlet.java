package com.vehiclerental.servlet;

import com.vehiclerental.dao.CustomerDAO;
import com.vehiclerental.dao.RentalDAO;
import com.vehiclerental.dao.VehicleDAO;
import com.vehiclerental.model.Customer;
import com.vehiclerental.model.Rental;
import com.vehiclerental.model.User;
import com.vehiclerental.model.Vehicle;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controller handling Customer Portal operations:
 * - /customer/dashboard
 * - /customer/rentals
 * - /customer/book
 */
@WebServlet(name = "CustomerPortalServlet", urlPatterns = {"/customer/dashboard", "/customer/rentals", "/customer/bookings", "/customer/book", "/customer/return"})
public class CustomerPortalServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private VehicleDAO vehicleDAO;
    private RentalDAO rentalDAO;
    private CustomerDAO customerDAO;

    @Override
    public void init() throws ServletException {
        vehicleDAO = new VehicleDAO();
        rentalDAO = new RentalDAO();
        customerDAO = new CustomerDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null || user.getCustomerId() == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        int customerId = user.getCustomerId();
        String path = request.getServletPath();

        if ("/customer/rentals".equals(path) || "/customer/bookings".equals(path)) {
            // Customer's comprehensive "My Bookings" module
            List<Rental> myRentals = rentalDAO.getRentalsByCustomerId(customerId);
            Customer customerDetails = customerDAO.getCustomerById(customerId);

            long activeCount = myRentals.stream().filter(Rental::isActive).count();
            long returnedCount = myRentals.stream().filter(Rental::isReturned).count();
            double totalSpent = myRentals.stream().mapToDouble(Rental::getTotalAmount).sum();

            String msg = request.getParameter("msg");
            if ("returned".equals(msg)) {
                request.setAttribute("successMessage", "Vehicle return completed successfully! The vehicle has been released back to our available fleet.");
            } else if ("booked".equals(msg)) {
                request.setAttribute("successMessage", "Booking confirmed! Your vehicle is active and ready for your trip.");
            }

            String error = request.getParameter("error");
            if ("missing_fields".equals(error)) {
                request.setAttribute("errorMessage", "Please provide the rental and return dates.");
            } else if ("unauthorized_return".equals(error)) {
                request.setAttribute("errorMessage", "That active rental could not be found for your account.");
            } else if ("return_failed".equals(error)) {
                request.setAttribute("errorMessage", "The vehicle could not be returned. Please try again.");
            }

            request.setAttribute("rentals", myRentals);
            request.setAttribute("customer", customerDetails);
            request.setAttribute("activeCount", activeCount);
            request.setAttribute("returnedCount", returnedCount);
            request.setAttribute("totalSpent", totalSpent);

            request.getRequestDispatcher("/customer/my-rentals.jsp").forward(request, response);
            return;
        }

        if ("/customer/book".equals(path)) {
            // Self-service booking form
            String vehicleIdParam = request.getParameter("vehicleId");
            if (vehicleIdParam != null && !vehicleIdParam.trim().isEmpty()) {
                try {
                    int vId = Integer.parseInt(vehicleIdParam.trim());
                    Vehicle selectedVehicle = vehicleDAO.getVehicleById(vId);
                    request.setAttribute("selectedVehicle", selectedVehicle);
                } catch (NumberFormatException ignored) {}
            }
            List<Vehicle> availableVehicles = vehicleDAO.getAvailableVehicles();
            request.setAttribute("availableVehicles", availableVehicles);
            String error = request.getParameter("error");
            if ("booking_failed".equals(error)) {
                request.setAttribute("errorMessage", "The booking could not be completed. Please select an available vehicle and try again.");
            }
            request.getRequestDispatcher("/customer/book.jsp").forward(request, response);
            return;
        }

        // Default: /customer/dashboard
        List<Rental> myRentals = rentalDAO.getRentalsByCustomerId(customerId);
        long activeCount = myRentals.stream().filter(Rental::isActive).count();
        int totalCount = myRentals.size();

        List<Vehicle> availableVehicles = vehicleDAO.getAvailableVehicles();

        Customer customerDetails = customerDAO.getCustomerById(customerId);
        request.setAttribute("customer", customerDetails);
        request.setAttribute("myRentals", myRentals);
        request.setAttribute("activeCount", activeCount);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("availableVehicles", availableVehicles);

        String msg = request.getParameter("msg");
        if ("welcome".equals(msg)) {
            request.setAttribute("successMessage", "Welcome to DriveEase! Your account is active.");
        } else if ("booked".equals(msg)) {
            request.setAttribute("successMessage", "Vehicle booked successfully! Check your rental details below.");
        }

        request.getRequestDispatcher("/customer/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null || user.getCustomerId() == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        int customerId = user.getCustomerId();
        String path = request.getServletPath();

        if ("/customer/return".equals(path)) {
            // Customer initiates vehicle return
            String rentalIdStr = request.getParameter("rentalId");
            String returnDateStr = request.getParameter("returnDate");

            if (rentalIdStr == null || returnDateStr == null ||
                rentalIdStr.trim().isEmpty() || returnDateStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/customer/bookings?error=missing_fields");
                return;
            }

            try {
                int rentalId = Integer.parseInt(rentalIdStr.trim());
                Rental rental = rentalDAO.getRentalById(rentalId);

                // Security constraint: Must belong to this customer and be ACTIVE
                if (rental == null || rental.getCustomerId() != customerId || !rental.isActive()) {
                    response.sendRedirect(request.getContextPath() + "/customer/bookings?error=unauthorized_return");
                    return;
                }

                LocalDate rentalStart = rental.getRentalDate().toLocalDate();
                LocalDate actualReturn = LocalDate.parse(returnDateStr.trim());

                if (actualReturn.isBefore(rentalStart)) {
                    actualReturn = rentalStart;
                }

                long days = ChronoUnit.DAYS.between(rentalStart, actualReturn);
                if (days <= 0) days = 1;
                double totalAmount = days * rental.getDailyRate();

                boolean success = rentalDAO.returnVehicle(rentalId, Date.valueOf(actualReturn), totalAmount);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/customer/bookings?msg=returned");
                } else {
                    response.sendRedirect(request.getContextPath() + "/customer/bookings?error=return_failed");
                }
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/customer/bookings?error=" + e.getMessage());
            }
            return;
        }

        if ("/customer/book".equals(path)) {
            String vehicleIdStr = request.getParameter("vehicleId");
            String rentalDateStr = request.getParameter("rentalDate");
            String returnDateStr = request.getParameter("returnDate");

            if (vehicleIdStr == null || rentalDateStr == null || returnDateStr == null ||
                vehicleIdStr.trim().isEmpty() || rentalDateStr.trim().isEmpty() || returnDateStr.trim().isEmpty()) {
                request.setAttribute("errorMessage", "All booking fields are required.");
                doGet(request, response);
                return;
            }

            try {
                int vehicleId = Integer.parseInt(vehicleIdStr.trim());
                Vehicle vehicle = vehicleDAO.getVehicleById(vehicleId);

                if (vehicle == null || !vehicle.isAvailable()) {
                    request.setAttribute("errorMessage", "Selected vehicle is currently unavailable.");
                    doGet(request, response);
                    return;
                }

                LocalDate rentalLocalDate = LocalDate.parse(rentalDateStr.trim());
                LocalDate returnLocalDate = LocalDate.parse(returnDateStr.trim());

                if (returnLocalDate.isBefore(rentalLocalDate)) {
                    request.setAttribute("errorMessage", "Expected return date cannot be before rental start date.");
                    doGet(request, response);
                    return;
                }

                long days = ChronoUnit.DAYS.between(rentalLocalDate, returnLocalDate);
                if (days <= 0) days = 1;
                double totalAmount = days * vehicle.getRentalPrice();

                Rental rental = new Rental();
                rental.setCustomerId(customerId);
                rental.setVehicleId(vehicleId);
                rental.setRentalDate(Date.valueOf(rentalLocalDate));
                rental.setReturnDate(Date.valueOf(returnLocalDate));
                rental.setTotalAmount(totalAmount);
                rental.setRentalStatus("ACTIVE");

                boolean created = rentalDAO.createRental(rental);
                if (created) {
                    response.sendRedirect(request.getContextPath() + "/customer/bookings?msg=booked");
                } else {
                    request.setAttribute("errorMessage", "Could not complete booking. Please try again.");
                    doGet(request, response);
                }
            } catch (Exception e) {
                request.setAttribute("errorMessage", "Invalid booking input: " + e.getMessage());
                doGet(request, response);
            }
        }
    }
}
