<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal Login | DriveEase Rentals</title>
    <c:url var="styleUrl" value="/css/style.css" />
    <link rel="stylesheet" href="${styleUrl}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <header class="navbar">
        <div class="nav-brand">
            <div class="brand-icon"><i class="fa-solid fa-car"></i></div>
            <span>DriveEase Rentals</span>
        </div>
        <c:url var="homeUrl" value="/" />
        <a class="nav-links" href="${homeUrl}"><i class="fa-solid fa-arrow-left"></i> Back to Fleet Catalog</a>
    </header>

    <main class="login-wrapper">
        <section class="login-card" style="max-width: 480px;">
            <div class="login-header">
                <div class="login-logo" id="portalLogo"><i class="fa-solid fa-shield-halved"></i></div>
                <h1 id="portalTitle">Portal Access</h1>
                <p id="portalSubtitle">Sign in to manage fleet operations or customer bookings.</p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error" role="alert"><c:out value="${errorMessage}" /></div>
            </c:if>
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success" role="status"><c:out value="${successMessage}" /></div>
            </c:if>

            <div class="role-switcher" role="tablist" aria-label="Account type">
                <button type="button" class="role-tab-btn" id="tabAdmin" onclick="setRole('admin')">
                    <i class="fa-solid fa-user-shield"></i> Admin
                </button>
                <button type="button" class="role-tab-btn" id="tabCustomer" onclick="setRole('customer')">
                    <i class="fa-solid fa-user"></i> Customer
                </button>
                <button type="button" class="role-tab-btn" id="tabRegister" onclick="setRole('register')">
                    <i class="fa-solid fa-user-plus"></i> Register
                </button>
            </div>

            <c:url var="loginUrl" value="/login" />
            <section id="panelAdmin" class="role-panel">
                <form action="${loginUrl}" method="post">
                    <input type="hidden" name="role" value="admin">
                    <div class="form-group">
                        <label for="adminUsername">Admin Username <span class="required-star">*</span></label>
                        <input type="text" id="adminUsername" name="username" value="<c:out value='${enteredUsername}' />"
                               autocomplete="username" required autofocus>
                    </div>
                    <div class="form-group">
                        <label for="adminPassword">Password <span class="required-star">*</span></label>
                        <input type="password" id="adminPassword" name="password" autocomplete="current-password" required>
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%; padding: 0.8rem; font-size: 1rem;">
                        <i class="fa-solid fa-right-to-bracket"></i> Sign In to Admin Dashboard
                    </button>
                </form>
            </section>

            <section id="panelCustomer" class="role-panel">
                <form action="${loginUrl}" method="post">
                    <input type="hidden" name="role" value="customer">
                    <div class="form-group">
                        <label for="custEmail">Customer Email <span class="required-star">*</span></label>
                        <input type="email" id="custEmail" name="username"
                               value="<c:out value='${enteredUsername}' />" autocomplete="email" required>
                    </div>
                    <div class="form-group">
                        <label for="custPassword">Password <span class="required-star">*</span></label>
                        <input type="password" id="custPassword" name="password" autocomplete="current-password" required>
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%; padding: 0.8rem; font-size: 1rem; background: #0284c7;">
                        <i class="fa-solid fa-car-side"></i> Sign In to Customer Portal
                    </button>
                </form>
                <p style="text-align: center; margin-top: 1.25rem; font-size: 0.875rem;">
                    New here? <a href="#" onclick="setRole('register'); return false;" style="font-weight: 600;">Create a customer account</a>
                </p>
            </section>

            <c:url var="registerUrl" value="/register" />
            <section id="panelRegister" class="role-panel">
                <form action="${registerUrl}" method="post">
                    <div class="form-group">
                        <label for="regName">Full Name <span class="required-star">*</span></label>
                        <input type="text" id="regName" name="name" maxlength="100"
                               value="<c:out value='${regName}' />" autocomplete="name" required>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="regPhone">Phone Number <span class="required-star">*</span></label>
                            <input type="tel" id="regPhone" name="phone" maxlength="15"
                                   value="<c:out value='${regPhone}' />" autocomplete="tel" required>
                        </div>
                        <div class="form-group">
                            <label for="regEmail">Email Address <span class="required-star">*</span></label>
                            <input type="email" id="regEmail" name="email" maxlength="100"
                                   value="<c:out value='${regEmail}' />" autocomplete="email" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="regPassword">Password <span class="required-star">*</span></label>
                        <input type="password" id="regPassword" name="password" minlength="6"
                               autocomplete="new-password" required>
                        <small>Use at least 6 characters.</small>
                    </div>
                    <div class="form-group">
                        <label for="regAddress">Residential Address</label>
                        <input type="text" id="regAddress" name="address" maxlength="255"
                               value="<c:out value='${regAddress}' />" autocomplete="street-address">
                    </div>
                    <button type="submit" class="btn btn-success" style="width: 100%; padding: 0.8rem; font-size: 1rem;">
                        <i class="fa-solid fa-user-plus"></i> Create Customer Account
                    </button>
                </form>
                <p style="text-align: center; margin-top: 1.25rem; font-size: 0.875rem;">
                    Already registered? <a href="#" onclick="setRole('customer'); return false;" style="font-weight: 600;">Sign in here</a>
                </p>
            </section>
        </section>
    </main>

    <script>
        function setRole(role) {
            ['admin', 'customer', 'register'].forEach(function (item) {
                document.getElementById('tab' + item[0].toUpperCase() + item.slice(1)).classList.toggle('active', item === role);
                document.getElementById('panel' + item[0].toUpperCase() + item.slice(1)).classList.toggle('active', item === role);
            });
        }
        setRole('<c:out value="${activeTab}" default="admin" />');
    </script>
</body>
</html>
