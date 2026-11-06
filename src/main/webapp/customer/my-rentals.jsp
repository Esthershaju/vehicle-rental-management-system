<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Bookings | DriveEase Rentals</title>
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
                <li><a href="<c:url value='/customer/bookings' />" class="active"><i class="fa-solid fa-clock-rotate-left"></i> My Bookings</a></li>
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
        <div class="page-header">
            <div class="page-title"><h1>My Rental History</h1><p>Review your active bookings and completed rentals.</p></div>
            <a href="<c:url value='/customer/book' />" class="btn btn-primary">Book a Vehicle</a>
        </div>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success" role="status"><c:out value="${successMessage}" /></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error" role="alert"><c:out value="${errorMessage}" /></div>
        </c:if>

        <section class="metrics-grid">
            <div class="metric-card"><div class="metric-info"><h3><c:out value="${activeCount}" /></h3><p>Active Rentals</p></div></div>
            <div class="metric-card"><div class="metric-info"><h3><c:out value="${returnedCount}" /></h3><p>Completed Rentals</p></div></div>
            <div class="metric-card"><div class="metric-info"><h3><c:out value="${totalSpent}" /></h3><p>Total Charges</p></div></div>
        </section>

        <section class="card">
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr><th>Rental #</th><th>Vehicle</th><th>Rental Start</th><th>Expected Return</th><th>Charge</th><th>Status</th><th>Return</th></tr>
                    </thead>
                    <tbody>
                        <c:forEach var="rental" items="${rentals}">
                            <tr>
                                <td><c:out value="${rental.rentalId}" /></td>
                                <td><strong><c:out value="${rental.vehicleName}" /></strong><br>
                                    <small><c:out value="${rental.vehicleNumber}" /> &bull; <c:out value="${rental.vehicleType}" /></small></td>
                                <td><c:out value="${rental.rentalDate}" /></td>
                                <td><c:out value="${rental.returnDate}" /></td>
                                <td><c:out value="${rental.formattedTotalAmount}" /></td>
                                <td><span class="badge ${rental.active ? 'badge-active' : 'badge-returned'}"><c:out value="${rental.rentalStatus}" /></span></td>
                                <td>
                                    <c:if test="${rental.active}">
                                        <form action="<c:url value='/customer/return' />" method="post">
                                            <input type="hidden" name="rentalId" value="<c:out value='${rental.rentalId}' />">
                                            <label for="returnDate-${rental.rentalId}">Return date</label>
                                            <input type="date" id="returnDate-${rental.rentalId}" name="returnDate" required>
                                            <button type="submit" class="btn btn-primary btn-sm">Return</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty rentals}">
                            <tr><td colspan="7">You have no bookings yet. Browse the fleet to get started.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</body>
</html>
