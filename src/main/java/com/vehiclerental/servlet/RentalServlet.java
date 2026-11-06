package com.vehiclerental.servlet;

import com.vehiclerental.dao.CustomerDAO;
import com.vehiclerental.dao.RentalDAO;
import com.vehiclerental.dao.VehicleDAO;
import com.vehiclerental.model.Customer;
import com.vehiclerental.model.Rental;
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
 * Servlet handling Vehicle Rentals, Returns, Calculations, and Searches.
 */
@WebServlet(name = "RentalServlet", urlPatterns = {"/rentals", "/rentals/*"})
public class RentalServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RentalDAO rentalDAO;
    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;

    @Override
    public void init() throws ServletException {
        rentalDAO = new RentalDAO();
        vehicleDAO = new VehicleDAO();
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
            case "new":
            case "add":
                showNewRentalForm(request, response);
                break;
            case "return":
                showReturnConfirmation(request, response);
                break;
            case "search":
                searchRentals(request, response);
                break;
            case "list":
            default:
                listRentals(request, response);
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
            case "create":
                createRental(request, response);
                break;
            case "processReturn":
                processReturn(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/rentals");
                break;
        }
    }

    // --- Action Handlers ---

    private void listRentals(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String filter = request.getParameter("filter");
        List<Rental> allRentals = rentalDAO.getAllRentals();
        
        if ("active".equalsIgnoreCase(filter)) {
            allRentals.removeIf(r -> !"ACTIVE".equalsIgnoreCase(r.getRentalStatus()));
            request.setAttribute("activeFilter", "active");
        } else if ("returned".equalsIgnoreCase(filter)) {
            allRentals.removeIf(r -> !"RETURNED".equalsIgnoreCase(r.getRentalStatus()));
            request.setAttribute("activeFilter", "returned");
        } else {
            request.setAttribute("activeFilter", "all");
        }

        request.setAttribute("rentalList", allRentals);
        request.getRequestDispatcher("/rentals/list.jsp").forward(request, response);
    }

    private void searchRentals(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Rental> rentalList;
        if (keyword != null && !keyword.trim().isEmpty()) {
            rentalList = rentalDAO.searchRentals(keyword.trim());
            request.setAttribute("keyword", keyword.trim());
        } else {
            rentalList = rentalDAO.getAllRentals();
        }
        request.setAttribute("rentalList", rentalList);
        request.setAttribute("activeFilter", "all");
        request.getRequestDispatcher("/rentals/list.jsp").forward(request, response);
    }

    private void showNewRentalForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Load customers and only AVAILABLE vehicles
        List<Customer> customerList = customerDAO.getAllCustomers();
        List<Vehicle> availableVehicles = vehicleDAO.getAvailableVehicles();

        request.setAttribute("customerList", customerList);
        request.setAttribute("vehicleList", availableVehicles);

        // Pre-select vehicle if vehicleId was passed in query parameter (e.g. from vehicle list or public catalog)
        String preSelectedVehicleId = request.getParameter("vehicleId");
        if (preSelectedVehicleId != null) {
            request.setAttribute("selectedVehicleId", preSelectedVehicleId);
        }

        // Set default rental date as today
        request.setAttribute("defaultRentalDate", LocalDate.now().toString());

        request.getRequestDispatcher("/rentals/add.jsp").forward(request, response);
    }

    private void createRental(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String customerIdStr = request.getParameter("customerId");
        String vehicleIdStr = request.getParameter("vehicleId");
        String rentalDateStr = request.getParameter("rentalDate");
        String returnDateStr = request.getParameter("returnDate");

        StringBuilder errors = new StringBuilder();

        int customerId = 0;
        int vehicleId = 0;

        try {
            customerId = Integer.parseInt(customerIdStr);
        } catch (Exception e) {
            errors.append("Please select a valid customer. ");
        }

        try {
            vehicleId = Integer.parseInt(vehicleIdStr);
        } catch (Exception e) {
            errors.append("Please select a valid vehicle. ");
        }

        if (isEmpty(rentalDateStr)) {
            errors.append("Rental start date is required. ");
        }

        Date rentalDate = null;
        Date returnDate = null;

        if (!isEmpty(rentalDateStr)) {
            try {
                rentalDate = Date.valueOf(rentalDateStr.trim());
            } catch (IllegalArgumentException e) {
                errors.append("Rental start date format is invalid. ");
            }
        }

        if (!isEmpty(returnDateStr)) {
            try {
                returnDate = Date.valueOf(returnDateStr.trim());
                if (rentalDate != null && returnDate.before(rentalDate)) {
                    errors.append("Return date cannot be earlier than rental date. ");
                }
            } catch (IllegalArgumentException e) {
                errors.append("Return date format is invalid. ");
            }
        }

        // Verify vehicle is AVAILABLE
        Vehicle vehicle = null;
        if (vehicleId > 0) {
            vehicle = vehicleDAO.getVehicleById(vehicleId);
            if (vehicle == null) {
                errors.append("Selected vehicle does not exist. ");
            } else if (!vehicle.isAvailable()) {
                errors.append("Selected vehicle is currently not available for rental. ");
            }
        }

        // Verify customer exists
        if (customerId > 0) {
            Customer customer = customerDAO.getCustomerById(customerId);
            if (customer == null) {
                errors.append("Selected customer does not exist. ");
            }
        }

        if (errors.length() > 0) {
            request.setAttribute("errorMessage", errors.toString());
            request.setAttribute("customerList", customerDAO.getAllCustomers());
            request.setAttribute("vehicleList", vehicleDAO.getAvailableVehicles());
            request.setAttribute("selectedCustomerId", customerIdStr);
            request.setAttribute("selectedVehicleId", vehicleIdStr);
            request.setAttribute("rentalDate", rentalDateStr);
            request.setAttribute("returnDate", returnDateStr);
            request.getRequestDispatcher("/rentals/add.jsp").forward(request, response);
            return;
        }

        // Calculate initial total amount based on duration
        double totalAmount = 0.0;
        if (returnDate != null) {
            long days = ChronoUnit.DAYS.between(rentalDate.toLocalDate(), returnDate.toLocalDate());
            if (days <= 0) days = 1; // Minimum 1 day
            totalAmount = days * vehicle.getRentalPrice();
        } else {
            // Open-ended rental, default to 1 day charge initially
            totalAmount = vehicle.getRentalPrice();
        }

        Rental rental = new Rental(customerId, vehicleId, rentalDate, returnDate, totalAmount, "ACTIVE");

        boolean success = rentalDAO.createRental(rental);
        if (success) {
            setFlashMessage(request, "success", "Rental #" + rental.getRentalId() + " created successfully! Vehicle status updated to RENTED.");
            response.sendRedirect(request.getContextPath() + "/rentals");
        } else {
            setFlashMessage(request, "error", "Failed to create rental. The vehicle may have just been rented by another user.");
            response.sendRedirect(request.getContextPath() + "/rentals?action=new");
        }
    }

    private void showReturnConfirmation(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String rentalIdStr = request.getParameter("id");
        if (rentalIdStr != null) {
            try {
                int rentalId = Integer.parseInt(rentalIdStr);
                Rental rental = rentalDAO.getRentalById(rentalId);
                if (rental != null) {
                    if (!rental.isActive()) {
                        setFlashMessage(request, "error", "This rental has already been marked as RETURNED.");
                        response.sendRedirect(request.getContextPath() + "/rentals");
                        return;
                    }

                    // Default return date to today
                    LocalDate today = LocalDate.now();
                    long days = ChronoUnit.DAYS.between(rental.getRentalDate().toLocalDate(), today);
                    if (days <= 0) days = 1;
                    double estimatedTotal = days * rental.getDailyRate();

                    request.setAttribute("rental", rental);
                    request.setAttribute("todayDate", today.toString());
                    request.setAttribute("calculatedDays", days);
                    request.setAttribute("calculatedAmount", estimatedTotal);
                    request.getRequestDispatcher("/rentals/return.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        setFlashMessage(request, "error", "Rental record not found.");
        response.sendRedirect(request.getContextPath() + "/rentals");
    }

    private void processReturn(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String rentalIdStr = request.getParameter("rentalId");
        String returnDateStr = request.getParameter("returnDate");
        String amountStr = request.getParameter("totalAmount");

        try {
            int rentalId = Integer.parseInt(rentalIdStr);
            Rental rental = rentalDAO.getRentalById(rentalId);
            if (rental == null || !rental.isActive()) {
                setFlashMessage(request, "error", "Rental is not active or could not be found.");
                response.sendRedirect(request.getContextPath() + "/rentals");
                return;
            }

            Date returnDate = (returnDateStr != null && !returnDateStr.trim().isEmpty())
                    ? Date.valueOf(returnDateStr.trim())
                    : Date.valueOf(LocalDate.now());

            if (returnDate.before(rental.getRentalDate())) {
                setFlashMessage(request, "error", "Return date cannot be earlier than rental date (" + rental.getRentalDate() + ").");
                response.sendRedirect(request.getContextPath() + "/rentals?action=return&id=" + rentalId);
                return;
            }

            // Recalculate amount accurately based on returnDate
            long days = ChronoUnit.DAYS.between(rental.getRentalDate().toLocalDate(), returnDate.toLocalDate());
            if (days <= 0) days = 1;
            double finalAmount = days * rental.getDailyRate();

            // If an explicit total was provided in form, use that if positive
            if (amountStr != null && !amountStr.trim().isEmpty()) {
                try {
                    double parsed = Double.parseDouble(amountStr.trim());
                    if (parsed >= 0) {
                        finalAmount = parsed;
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            boolean success = rentalDAO.returnVehicle(rentalId, returnDate, finalAmount);
            if (success) {
                setFlashMessage(request, "success", "Vehicle successfully returned! Rental status marked RETURNED and vehicle marked AVAILABLE.");
            } else {
                setFlashMessage(request, "error", "Failed to process return. Please check database transaction logs.");
            }

        } catch (Exception e) {
            setFlashMessage(request, "error", "Invalid return parameters: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/rentals");
    }

    // --- Helpers ---

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
