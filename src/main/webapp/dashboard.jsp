<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | DriveEase Rentals</title>
    <c:url var="styleUrl" value="/css/style.css" />
    <link rel="stylesheet" href="${styleUrl}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <header class="navbar">
        <div class="nav-brand"><div class="brand-icon"><i class="fa-solid fa-car"></i></div><span>DriveEase Rentals</span></div>
        <nav>
            <ul class="nav-links">
                <li><a href="<c:url value='/dashboard' />" class="active"><i class="fa-solid fa-gauge"></i> Dashboard</a></li>
                <li><a href="<c:url value='/vehicles' />"><i class="fa-solid fa-car-side"></i> Vehicles</a></li>
                <li><a href="<c:url value='/customers' />"><i class="fa-solid fa-users"></i> Customers</a></li>
                <li><a href="<c:url value='/rentals' />"><i class="fa-solid fa-file-invoice-dollar"></i> Rentals</a></li>
            </ul>
        </nav>
        <div class="nav-user">
            <div class="user-badge">
                <div class="user-avatar"><i class="fa-solid fa-user-shield"></i></div>
                <span><c:out value="${sessionScope.displayName}" /></span>
            </div>
            <form action="<c:url value='/logout' />" method="post">
                <button type="submit" class="btn btn-logout"><i class="fa-solid fa-right-from-bracket"></i> Logout</button>
            </form>
        </div>
    </header>

    <main class="container">
        <div class="page-header">
            <div class="page-title">
                <h1>Rental Operations Dashboard</h1>
                <p>Live vehicle inventory, customer, and rental metrics.</p>
            </div>
            <a href="<c:url value='/' />" class="btn btn-secondary btn-sm">View Public Catalog</a>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error" role="alert"><c:out value="${errorMessage}" /></div>
        </c:if>

        <div class="metrics-grid">
            <div class="metric-card card-vehicles"><div class="metric-info"><h3>Total Fleet</h3><div class="metric-value"><c:out value="${totalVehicles}" /></div></div><div class="metric-icon"><i class="fa-solid fa-car"></i></div></div>
            <div class="metric-card card-available"><div class="metric-info"><h3>Available Vehicles</h3><div class="metric-value"><c:out value="${availableVehicles}" /></div></div><div class="metric-icon"><i class="fa-solid fa-circle-check"></i></div></div>
            <div class="metric-card card-rented"><div class="metric-info"><h3>Rented Vehicles</h3><div class="metric-value"><c:out value="${rentedVehicles}" /></div></div><div class="metric-icon"><i class="fa-solid fa-clock-rotate-left"></i></div></div>
            <div class="metric-card card-customers"><div class="metric-info"><h3>Total Customers</h3><div class="metric-value"><c:out value="${totalCustomers}" /></div></div><div class="metric-icon"><i class="fa-solid fa-users"></i></div></div>
            <div class="metric-card card-rentals"><div class="metric-info"><h3>Total Rentals</h3><div class="metric-value"><c:out value="${totalRentals}" /></div></div><div class="metric-icon"><i class="fa-solid fa-file-invoice"></i></div></div>
        </div>

        <section class="quick-actions" aria-label="Quick actions">
            <a href="<c:url value='/vehicles?action=add' />" class="action-btn"><i class="fa-solid fa-circle-plus"></i> Add New Vehicle</a>
            <a href="<c:url value='/customers?action=add' />" class="action-btn"><i class="fa-solid fa-user-plus"></i> Register Customer</a>
            <a href="<c:url value='/rentals?action=add' />" class="action-btn"><i class="fa-solid fa-key"></i> Create New Rental</a>
            <span class="action-btn"><i class="fa-solid fa-clock"></i> Active Rentals: <c:out value="${activeRentals}" /></span>
        </section>

        <section class="card">
            <div class="card-header">
                <h2>Recent Rental Transactions</h2>
                <a href="<c:url value='/rentals' />" class="btn btn-secondary btn-sm">View All Rentals</a>
            </div>
            <div class="card-body" style="padding: 0;">
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr><th>Rental ID</th><th>Customer</th><th>Vehicle</th><th>Rental Date</th><th>Return Date</th><th>Total</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="rental" items="${recentRentals}">
                                <tr>
                                    <td><c:out value="${rental.rentalId}" /></td>
                                    <td><c:out value="${rental.customerName}" /></td>
                                    <td><c:out value="${rental.vehicleName}" /> (<c:out value="${rental.vehicleNumber}" />)</td>
                                    <td><c:out value="${rental.rentalDate}" /></td>
                                    <td><c:out value="${rental.returnDate}" /></td>
                                    <td><c:out value="${rental.formattedTotalAmount}" /></td>
                                    <td><span class="badge ${rental.active ? 'badge-active' : 'badge-returned'}"><c:out value="${rental.rentalStatus}" /></span></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty recentRentals}">
                                <tr><td colspan="7">No rental transactions yet.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    </main>
</body>
</html>
