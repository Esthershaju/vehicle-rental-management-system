<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Dashboard | DriveEase Rentals</title>
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
                <li><a href="<c:url value='/customer/dashboard' />" class="active"><i class="fa-solid fa-gauge"></i> My Dashboard</a></li>
                <li><a href="<c:url value='/customer/bookings' />"><i class="fa-solid fa-clock-rotate-left"></i> My Bookings</a></li>
                <li><a href="<c:url value='/customer/book' />"><i class="fa-solid fa-plus-circle"></i> Book Vehicle</a></li>
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
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success" role="status"><c:out value="${successMessage}" /></div>
        </c:if>
        <div class="customer-hero" style="margin-top: 1.5rem;">
            <div>
                <h1>Hello, <c:out value="${sessionScope.displayName}" />!</h1>
                <p>Manage your bookings or browse vehicles available to rent.</p>
            </div>
            <a href="<c:url value='/customer/book' />" class="btn btn-success">Rent a Vehicle</a>
        </div>

        <div class="metrics-grid">
            <div class="metric-card"><div class="metric-info"><h3><c:out value="${activeCount}" /></h3><p>Active Rentals</p></div></div>
            <div class="metric-card"><div class="metric-info"><h3><c:out value="${totalCount}" /></h3><p>Total Bookings</p></div></div>
            <div class="metric-card"><div class="metric-info"><h3><c:out value="${availableVehicles.size()}" /></h3><p>Vehicles Available</p></div></div>
        </div>

        <section class="card" style="margin-top: 2rem;">
            <div class="card-header">
                <h2>Your Recent Rentals</h2>
                <a href="<c:url value='/customer/bookings' />" class="btn btn-secondary btn-sm">View All Bookings</a>
            </div>
            <div class="table-responsive">
                <table>
                    <thead><tr><th>Rental #</th><th>Vehicle</th><th>Start Date</th><th>Return Date</th><th>Amount</th><th>Status</th></tr></thead>
                    <tbody>
                        <c:forEach var="rental" items="${myRentals}">
                            <tr>
                                <td><c:out value="${rental.rentalId}" /></td>
                                <td><c:out value="${rental.vehicleName}" /></td>
                                <td><c:out value="${rental.rentalDate}" /></td>
                                <td><c:out value="${rental.returnDate}" /></td>
                                <td><c:out value="${rental.formattedTotalAmount}" /></td>
                                <td><c:out value="${rental.rentalStatus}" /></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty myRentals}">
                            <tr><td colspan="6">You have no bookings yet. Browse the fleet to get started.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="card" style="margin-top: 2rem; margin-bottom: 3rem;">
            <div class="card-header"><h2>Available Vehicles</h2></div>
            <div class="vehicle-catalog-grid" style="margin-top: 1rem;">
                <c:forEach var="vehicle" items="${availableVehicles}">
                    <article class="vehicle-card">
                        <div class="vehicle-card-visual">
                            <i class="fa-solid fa-car-side"></i>
                            <span class="type-pill"><c:out value="${vehicle.vehicleType}" /></span>
                            <span class="badge badge-available">AVAILABLE</span>
                        </div>
                        <div class="vehicle-card-content">
                            <h3 class="vehicle-card-title"><c:out value="${vehicle.vehicleName}" /></h3>
                            <p class="vehicle-card-specs"><c:out value="${vehicle.brand}" /> <c:out value="${vehicle.model}" /></p>
                            <div class="vehicle-card-footer">
                                <div class="vehicle-price"><c:out value="${vehicle.formattedPrice}" /> <span>/ day</span></div>
                                <a href="<c:url value='/customer/book'><c:param name='vehicleId' value='${vehicle.vehicleId}' /></c:url>" class="btn btn-primary btn-sm">Book</a>
                            </div>
                        </div>
                    </article>
                </c:forEach>
                <c:if test="${empty availableVehicles}">
                    <p>No vehicles are currently available.</p>
                </c:if>
            </div>
        </section>
    </main>
</body>
</html>
