let created_expenses;
let name_field;
let price_field;
let category_field
let category_text;
let date_field;
const monthAbbreviations = [
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
];
let csrfToken;
let csrfHeader;

document.addEventListener("DOMContentLoaded", () => {
    csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    created_expenses = document.getElementById("created-expenses");
    name_field = document.getElementById("expenseName");
    price_field = document.getElementById("expensePrice");
    category_field = document.getElementById("expenseCategoryDropdown");
    date_field = document.getElementById("dateInput");
    category_text = document.getElementById("expenseCategorySpan");
    resetFields();

    // Event listener to only allow 0-9 and one . input in the price field
    price_field.addEventListener("input", (event) => {
        let value = price_field.value;

        const valid = value.replace(/[^0-9.]/g, '');

        const firstDotIndex = valid.indexOf(".");
        const cleaned = firstDotIndex !== -1
            ? valid.slice(0, firstDotIndex + 1) + valid.slice(firstDotIndex + 1).replace(/\./g, "")
            : valid;

        if (value !== cleaned) {
            price_field.value = cleaned;
        }
    });
})

// Set the selected category when clicked on the dropdown
function selectCategory(element) {
    const selectedText = element.textContent;
    const selectedValue = element.getAttribute("data-value");

    document.getElementById("expenseCategorySpan").textContent = selectedText;
    category_text.classList.remove(category_text.classList[category_text.classList.length - 1]);
    category_text.classList.add(selectedValue.toLowerCase().replace(/\s+/g, '-'));
    document.getElementById("stateInput").value = selectedValue;
}

// Reset all fields to default
function resetFields() {
    name_field.value = "";
    price_field.value = "";
    category_text.textContent = "Miscellaneous"
    if (category_text.classList) {
        category_text.classList.remove(category_text.classList[category_text.classList.length - 1]);
        category_text.classList.add('miscellaneous');
    }
    date_field.value = new Date().toISOString().slice(0, 10);
    clearAllErrors();
}

// Create preview of expense when add is clicked
function createPreview(name, price, category, date) {
    const expense_preview =  document.createElement("div");
    const inner_div = document.createElement("div");
    const title_div = document.createElement("div");
    const info_delete_div = document.createElement("div");
    const info_div = document.createElement("div");
    const name_wrap = document.createElement("div");
    const date_wrap = document.createElement("div");

    const delete_button = document.createElement("button");

    const expense_name = document.createElement("span");
    const price_badge = document.createElement("span");
    const category_badge = document.createElement("span");
    const date_badge = document.createElement("span");

    const day = date.slice(8);
    const month = date.slice(5, 7);
    const year = date.slice(0, 4);

    expense_name.textContent = name;
    price_badge.textContent = "$" + parseFloat(price).toFixed(2);
    category_badge.textContent = category;
    date_badge.textContent = monthAbbreviations[month - 1] + " " + day + ", " + year;

    price_badge.className = "badge bg-secondary";
    category_badge.className = "badge";
    category_badge.classList.add(category.toLowerCase().trim().replace(/\s+/g, '-'))
    date_badge.className = "badge bg-secondary";
    info_div.className = "d-flex align-items-center gap-2 mt-2 mt-sm-0";
    delete_button.className = "button button-red";
    title_div.className = "d-flex justify-content-between align-items-start mb-2";
    name_wrap.className = "text-break";
    date_wrap.className = "ms-2";
    info_delete_div.className = "d-flex justify-content-between align-items-center flex-wrap mb-2";
    expense_preview.className = "renovation-card"
    inner_div.className = "card-body"

    name_wrap.style.flex = "1";

    expense_preview.setAttribute("data-date", date);

    delete_button.textContent = "Delete";
    delete_button.addEventListener("click", () => {
        delete_button.parentElement.parentElement.parentElement.remove();
    });

    name_wrap.appendChild(expense_name);

    date_wrap.appendChild(date_badge);

    title_div.appendChild(name_wrap);
    title_div.appendChild(date_wrap);

    info_div.appendChild(category_badge);
    info_div.appendChild(price_badge);

    inner_div.appendChild(title_div);
    inner_div.appendChild(info_delete_div);

    info_delete_div.appendChild(info_div);
    info_delete_div.appendChild(delete_button);

    expense_preview.appendChild(inner_div);

    created_expenses.appendChild(expense_preview);
}


function parseLocalDate(dateStr) {
    const [year, month, day] = dateStr.split("-").map(Number);
    return new Date(year, month - 1, day);
}

// Chatgpt helped generate as was implemented in another task but in java instead of Javascript, chatgpt created similar
// functionality before other some other changes
function validateExpense(expenseName, expensePrice, expenseDateStr) {
    const MAX_TITLE_LENGTH = 64;
    const nameRegex = /^(?=.*[\p{L}\p{N}])[\p{L}\p{N} .'-]+$/u;
    const priceRegex = /^\d+(\.\d{1,2})?$/;
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;

    const errors = {
        name: "",
        price: "",
        date: ""
    };

    if (!expenseName || expenseName.trim() === "") {
        errors.name = "Expense name cannot be empty";
    } else if (!nameRegex.test(expenseName)) {
        errors.name = "Expense name must only include letters, numbers, spaces, dots, hyphens, or apostrophes, and must contain at least one letter or number";
    } else if (expenseName.length > MAX_TITLE_LENGTH) {
        errors.name = `Expense name must be ${MAX_TITLE_LENGTH} characters or less`;
    }

    if (!expensePrice || expensePrice.trim() === "") {
        errors.price = "Price cannot be empty";
    } else if (!priceRegex.test(expensePrice)) {
        errors.price = "Price must be a positive number in the form '5.99'";
    } else if (parseFloat(expensePrice) < 0) {
        errors.price = "Price must be a positive number in the form '5.99'";
    } else if (parseFloat(expensePrice) >= 10000000) {
        errors.price = "Price must be less than $10,000,000.00";
    }

    if (!dateRegex.test(expenseDateStr)) {
        errors.date = "Date is not in valid format, DD/MM/YYYY";
    } else {
        const expenseDate = parseLocalDate(expenseDateStr);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (expenseDate > today) {
            errors.date = "Expense date must be in the past";
        }
    }
    return errors;
}

function displayErrors(errors) {
    displayFieldError(name_field, errors.name);
    displayFieldError(price_field, errors.price);
    displayFieldError(date_field, errors.date);
}

// Display error for relevant field when needed, error html was copied from errorMessage fragment for consistent
// form errors across the app
function displayFieldError(inputElement, errorMessage) {
    const existingError = inputElement.parentElement.querySelector(".error-message");
    if (existingError) existingError.remove();

    if (errorMessage !== "") {
        inputElement.classList.add("error");
    } else {
        inputElement.classList.remove("error");
    }

    if (errorMessage !== "") {
        const errorDiv = document.createElement("div");
        errorDiv.className = "error-message";

        // from errorMessageFragment.html for consistent errors when incorrect input provided
        errorDiv.innerHTML = `
            <svg fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" style="width: 1em; height: 1em; margin-right: 0.25em;">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" x2="12" y1="8" y2="12"/>
                <line x1="12" x2="12.01" y1="16" y2="16"/>
            </svg>
            <span>${errorMessage}</span>
        `;

        inputElement.parentElement.appendChild(errorDiv);
    }
}

// Clears errors
function clearAllErrors() {
    const inputs = [name_field, price_field, date_field];

    inputs.forEach(input => {
        input.classList.remove("error");

        const existingError = input.parentElement.querySelector(".error-message");
        if (existingError) existingError.remove();
    });
}

// Error message for Add to task button
function showAddExpenseError(message) {
    const errorDiv = document.getElementById("add-expense-error");
    errorDiv.textContent = message;
    errorDiv.style.display = "block";
}

function clearAddExpenseError() {
    const errorDiv = document.getElementById("add-expense-error");
    errorDiv.textContent = "";
    errorDiv.style.display = "none";
}

// Try to create preview when add is clicked
function onAddClicked() {
    let name = name_field.value;
    let price = price_field.value;
    let category = category_text.textContent;
    let date = date_field.value;
    const errors = validateExpense(name, price, date);
    if (errors.date !== "" || errors.name !== "" || errors.price !== "") {
        displayErrors(errors);
    } else {
        createPreview(name, price, category, date);
        resetFields();
        clearAddExpenseError();
    }
}

// Try to submit all previewed expenses, sends post request and if successful redirects back to task page
function onAddToTaskClicked() {
    clearAddExpenseError();

    const expenses = Array.from(created_expenses.children).map(exp => {
        const name = exp.querySelector("div.card-body div span").textContent;
        const date = exp.getAttribute("data-date");

        const badges = exp.querySelectorAll(".badge");
        const category = badges[1].textContent;
        const price = badges[2].textContent.replace("$", "");

        return { name, date, category, price: parseFloat(price) };
    });

    if (expenses.length === 0) {
        showAddExpenseError("You must create one or more expenses to add to task.");
        return;
    }

    fetch(`${baseUrl}/myRenovations/${renoId}/addTaskExpenses/${taskId}`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(expenses)
    })
        .then(response => response.text())
        .then(redirectUrl => {
            window.location.href = baseUrl + '/' + redirectUrl;
        })
        .catch(error => {
            showAddExpenseError("Unable to add your expenses, please try again later.");
        });
}

