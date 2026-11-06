/**
 * Vehicle Rental Management System
 * Client-side Form Validation & Interactive Rental Calculator
 */

document.addEventListener('DOMContentLoaded', function () {

    // ==========================================
    // 1. Login Form Validation
    // ==========================================
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function (e) {
            const usernameInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');

            let isValid = true;
            clearErrors();

            if (!usernameInput.value.trim()) {
                showError(usernameInput, 'Username is required.');
                isValid = false;
            }

            if (!passwordInput.value.trim()) {
                showError(passwordInput, 'Password is required.');
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
            }
        });
    }

    // ==========================================
    // 2. Vehicle Form Validation (Add / Edit)
    // ==========================================
    const vehicleForm = document.getElementById('vehicleForm');
    if (vehicleForm) {
        vehicleForm.addEventListener('submit', function (e) {
            const vehicleNumber = document.getElementById('vehicleNumber');
            const vehicleName = document.getElementById('vehicleName');
            const vehicleType = document.getElementById('vehicleType');
            const rentalPrice = document.getElementById('rentalPrice');

            let isValid = true;
            clearErrors();

            if (!vehicleNumber.value.trim()) {
                showError(vehicleNumber, 'Vehicle Number is required (e.g. VH-101).');
                isValid = false;
            }

            if (!vehicleName.value.trim()) {
                showError(vehicleName, 'Vehicle Name is required.');
                isValid = false;
            }

            if (!vehicleType.value.trim()) {
                showError(vehicleType, 'Please select a vehicle category.');
                isValid = false;
            }

            const priceVal = parseFloat(rentalPrice.value);
            if (isNaN(priceVal) || priceVal <= 0) {
                showError(rentalPrice, 'Rental price must be a positive number greater than 0.');
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
            }
        });
    }

    // ==========================================
    // 3. Customer Form Validation (Add / Edit)
    // ==========================================
    const customerForm = document.getElementById('customerForm');
    if (customerForm) {
        customerForm.addEventListener('submit', function (e) {
            const name = document.getElementById('name');
            const phone = document.getElementById('phone');
            const email = document.getElementById('email');

            let isValid = true;
            clearErrors();

            if (!name.value.trim()) {
                showError(name, 'Customer Name is required.');
                isValid = false;
            }

            const phonePattern = /^[0-9+\-\s()]{7,20}$/;
            if (!phone.value.trim()) {
                showError(phone, 'Phone number is required.');
                isValid = false;
            } else if (!phonePattern.test(phone.value.trim())) {
                showError(phone, 'Please enter a valid phone number (digits and dashes).');
                isValid = false;
            }

            const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
            if (!email.value.trim()) {
                showError(email, 'Email address is required.');
                isValid = false;
            } else if (!emailPattern.test(email.value.trim())) {
                showError(email, 'Please enter a valid email address (e.g. user@example.com).');
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
            }
        });
    }

    // ==========================================
    // 4. Rental Form Validation & Live Dynamic Calculation
    // ==========================================
    const rentalForm = document.getElementById('rentalForm');
    if (rentalForm) {
        const vehicleSelect = document.getElementById('vehicleId');
        const rentalDateInput = document.getElementById('rentalDate');
        const returnDateInput = document.getElementById('returnDate');
        const daysDisplay = document.getElementById('rentalDaysDisplay');
        const totalDisplay = document.getElementById('rentalTotalDisplay');

        function updateRentalEstimate() {
            if (!vehicleSelect || !rentalDateInput) return;

            const selectedOption = vehicleSelect.options[vehicleSelect.selectedIndex];
            const dailyRate = selectedOption ? parseFloat(selectedOption.getAttribute('data-price') || '0') : 0;
            const rentalDate = rentalDateInput.value ? new Date(rentalDateInput.value) : null;
            const returnDate = (returnDateInput && returnDateInput.value) ? new Date(returnDateInput.value) : null;

            let days = 1;
            if (rentalDate && returnDate && returnDate >= rentalDate) {
                const diffTime = Math.abs(returnDate - rentalDate);
                days = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                if (days <= 0) days = 1;
            }

            const total = days * dailyRate;

            if (daysDisplay) {
                daysDisplay.textContent = days + (days === 1 ? ' day' : ' days');
            }
            if (totalDisplay) {
                totalDisplay.textContent = '₹' + total.toFixed(2);
            }
        }

        if (vehicleSelect) vehicleSelect.addEventListener('change', updateRentalEstimate);
        if (rentalDateInput) rentalDateInput.addEventListener('change', updateRentalEstimate);
        if (returnDateInput) returnDateInput.addEventListener('change', updateRentalEstimate);

        // Run calculation once on page load to initialize
        updateRentalEstimate();

        rentalForm.addEventListener('submit', function (e) {
            const customerSelect = document.getElementById('customerId');
            let isValid = true;
            clearErrors();

            if (!customerSelect.value.trim()) {
                showError(customerSelect, 'Please select a customer.');
                isValid = false;
            }

            if (!vehicleSelect.value.trim()) {
                showError(vehicleSelect, 'Please select an available vehicle.');
                isValid = false;
            }

            if (!rentalDateInput.value.trim()) {
                showError(rentalDateInput, 'Rental start date is required.');
                isValid = false;
            }

            if (returnDateInput && returnDateInput.value.trim()) {
                const start = new Date(rentalDateInput.value);
                const end = new Date(returnDateInput.value);
                if (end < start) {
                    showError(returnDateInput, 'Return date cannot be earlier than rental date.');
                    isValid = false;
                }
            }

            if (!isValid) {
                e.preventDefault();
            }
        });
    }

    // ==========================================
    // 5. Return Vehicle Live Calculation
    // ==========================================
    const returnForm = document.getElementById('returnForm');
    if (returnForm) {
        const returnDateInput = document.getElementById('returnDate');
        const dailyRate = parseFloat(returnForm.getAttribute('data-daily-rate') || '0');
        const rentalDateStr = returnForm.getAttribute('data-rental-date');
        const calculatedDaysDisplay = document.getElementById('returnDaysDisplay');
        const calculatedAmountInput = document.getElementById('totalAmount');

        if (returnDateInput && rentalDateStr) {
            returnDateInput.addEventListener('change', function () {
                const startDate = new Date(rentalDateStr);
                const endDate = new Date(returnDateInput.value);

                if (endDate >= startDate) {
                    const diffTime = Math.abs(endDate - startDate);
                    let days = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                    if (days <= 0) days = 1;
                    const finalAmount = days * dailyRate;

                    if (calculatedDaysDisplay) {
                        calculatedDaysDisplay.textContent = days + (days === 1 ? ' day' : ' days');
                    }
                    if (calculatedAmountInput) {
                        calculatedAmountInput.value = finalAmount.toFixed(2);
                    }
                }
            });
        }
    }

    // --- Helper Validation Functions ---

    function showError(inputElement, message) {
        inputElement.style.borderColor = '#ef4444';
        const errorDiv = document.createElement('div');
        errorDiv.className = 'form-error js-error';
        errorDiv.textContent = message;
        inputElement.parentNode.appendChild(errorDiv);
    }

    function clearErrors() {
        const errors = document.querySelectorAll('.js-error');
        errors.forEach(function (err) {
            err.remove();
        });
        const inputs = document.querySelectorAll('input, select, textarea');
        inputs.forEach(function (input) {
            input.style.borderColor = '';
        });
    }
});
