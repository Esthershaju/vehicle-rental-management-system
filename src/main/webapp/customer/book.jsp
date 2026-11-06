<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Book a Vehicle | DriveEase Rentals</title>
    <c:url var="styleUrl" value="/css/style.css" />
    <link rel="stylesheet" href="${styleUrl}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <header class="navbar">
        <div class="nav-brand"><div class="brand-icon"><i class="fa-solid fa-car"></i></div><span>DriveEase Rentals</span></div>
        <nav>
            <ul class="nav-links">
                <li><a href="<c:url value='/' />"><i class="fa-solid fa-house"></i> Fleet Catalog</a></li>
                <li><a href="<c:url value='/customer/dashboard' />"><i class="fa-solid fa-gauge"></i> My Dashboard</a></li>
                <li><a href="<c:url value='/customer/bookings' />"><i class="fa-solid fa-clock-rotate-left"></i> My Bookings</a></li>
                <li><a href="<c:url value='/customer/book' />" class="active"><i class="fa-solid fa-plus-circle"></i> Book Vehicle</a></li>
            </ul>
        </nav>
        <div class="nav-user">
            <span><c:out value="${sessionScope.displayName}" /></span>
            <form action="<c:url value='/logout' />" method="post">
                <button type="submit" class="btn btn-secondary btn-sm"><i class="fa-solid fa-right-from-bracket"></i> Logout</button>
            </form>
        </div>
    </header>

    <main class="container">
        <div class="page-header"><div class="page-title"><h1>Book a Vehicle</h1><p>Choose an available vehicle and your rental dates.</p></div></div>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error" role="alert"><c:out value="${errorMessage}" /></div>
        </c:if>
        <section class="card form-card">
            <form action="<c:url value='/customer/book' />" method="post" class="form-grid">
                <div class="form-group">
                    <label for="vehicleId">Select Vehicle <span class="required-star">*</span></label>
                    <select id="vehicleId" name="vehicleId" required>
                        <option value="">Choose an available vehicle</option>
                        <c:forEach var="vehicle" items="${availableVehicles}">
                            <option value="<c:out value='${vehicle.vehicleId}' />"
                                <c:if test="${not empty selectedVehicle and selectedVehicle.vehicleId == vehicle.vehicleId}">selected</c:if>>
                                <c:out value="${vehicle.vehicleName}" /> — <c:out value="${vehicle.formattedPrice}" /> per day
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label for="rentalDate">Rental Start Date <span class="required-star">*</span></label>
                    <input type="date" id="rentalDate" name="rentalDate" required>
                </div>
                <div class="form-group">
                    <label for="returnDate">Expected Return Date <span class="required-star">*</span></label>
                    <input type="date" id="returnDate" name="returnDate" required>
                </div>
                <div class="form-actions" style="grid-column: 1 / -1;">
                    <button type="submit" class="btn btn-primary"><i class="fa-solid fa-key"></i> Confirm Booking</button>
                    <a href="<c:url value='/customer/dashboard' />" class="btn btn-secondary">Cancel</a>
                </div>
                <c:if test="${empty availableVehicles}">
                    <p>No vehicles are currently available to book.</p>
                </c:if>
            </form>
        </section>
    </main>
</body>
</html>
