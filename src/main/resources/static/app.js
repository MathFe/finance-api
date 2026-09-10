const API_URL = '/api';

// State
let token = localStorage.getItem('token') || null;
let user = JSON.parse(localStorage.getItem('user')) || null;
let categories = [];
let transactions = [];

// Elements
const authSection = document.getElementById('auth-section');
const dashboardSection = document.getElementById('dashboard-section');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const logoutBtn = document.getElementById('logout-btn');
const userGreeting = document.getElementById('user-greeting');
const balanceAmount = document.getElementById('balance-amount');
const transCategorySelect = document.getElementById('trans-category');
const transactionsList = document.getElementById('transactions-list');
const addCategoryForm = document.getElementById('add-category-form');
const addTransactionForm = document.getElementById('add-transaction-form');

// Initialize
function init() {
    if (token) {
        showDashboard();
        loadDashboardData();
    } else {
        showAuth();
    }
}

// UI Navigation
function showAuth() {
    authSection.classList.add('active');
    authSection.classList.remove('hidden');
    dashboardSection.classList.add('hidden');
    dashboardSection.classList.remove('active');
}

function showDashboard() {
    authSection.classList.add('hidden');
    authSection.classList.remove('active');
    dashboardSection.classList.add('active');
    dashboardSection.classList.remove('hidden');
    userGreeting.textContent = `Hello, ${user.name}`;
}

// Auth API
async function apiCall(endpoint, method = 'GET', body = null) {
    const headers = {
        'Content-Type': 'application/json'
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const options = {
        method,
        headers
    };
    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_URL}${endpoint}`, options);
    if (!response.ok) {
        const err = await response.text();
        throw new Error(err || 'API Error');
    }

    // Some endpoints might return empty body
    const text = await response.text();
    return text ? JSON.parse(text) : {};
}

// Event Listeners
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    const errorEl = document.getElementById('login-error');
    errorEl.textContent = '';

    try {
        const data = await apiCall('/auth/login', 'POST', { email, password, name: 'dummy' }); // name needed by dto but ignored in login
        token = data.token;
        user = data.user;
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));

        loginForm.reset();
        showDashboard();
        loadDashboardData();
    } catch (err) {
        errorEl.textContent = err.message;
    }
});

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const errorEl = document.getElementById('reg-error');
    errorEl.textContent = '';

    try {
        await apiCall('/auth/register', 'POST', { name, email, password });
        alert('Registration successful! Please login.');
        registerForm.reset();
    } catch (err) {
        errorEl.textContent = err.message;
    }
});

logoutBtn.addEventListener('click', () => {
    token = null;
    user = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    showAuth();
});

addCategoryForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('cat-name').value;
    const type = document.getElementById('cat-type').value;
    const color = document.getElementById('cat-color').value;
    const msgEl = document.getElementById('cat-msg');

    try {
        await apiCall('/categories', 'POST', { name, type, color });
        msgEl.textContent = 'Category added!';
        addCategoryForm.reset();
        setTimeout(() => msgEl.textContent = '', 3000);
        await loadCategories(); // Refresh select list
    } catch (err) {
        msgEl.textContent = 'Error: ' + err.message;
        msgEl.style.color = '#ff6b6b';
    }
});

addTransactionForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const description = document.getElementById('trans-desc').value;
    const amount = parseFloat(document.getElementById('trans-amount').value);
    const categoryId = parseInt(document.getElementById('trans-category').value);
    const type = document.getElementById('trans-type').value;
    const transactionDate = document.getElementById('trans-date').value;
    const msgEl = document.getElementById('trans-msg');

    if (!categoryId) {
        msgEl.textContent = 'Please select a category';
        msgEl.style.color = '#ff6b6b';
        return;
    }

    try {
        await apiCall('/transactions', 'POST', { description, amount, categoryId, type, transactionDate });
        msgEl.textContent = 'Transaction added!';
        msgEl.style.color = '#51cf66';
        addTransactionForm.reset();
        setTimeout(() => msgEl.textContent = '', 3000);
        await loadTransactions(); // Refresh list and balance
    } catch (err) {
        msgEl.textContent = 'Error: ' + err.message;
        msgEl.style.color = '#ff6b6b';
    }
});

// Data Loading
async function loadDashboardData() {
    await loadCategories();
    await loadTransactions();
}

async function loadCategories() {
    try {
        categories = await apiCall('/categories');

        // Update select
        transCategorySelect.innerHTML = '<option value="">Select Category...</option>';
        categories.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat.id;
            option.textContent = `${cat.name} (${cat.type})`;
            transCategorySelect.appendChild(option);
        });
    } catch (err) {
        console.error('Failed to load categories', err);
    }
}

async function loadTransactions() {
    try {
        transactions = await apiCall('/transactions');

        // Sort by date desc
        transactions.sort((a, b) => new Date(b.transactionDate) - new Date(a.transactionDate));

        // Calculate balance
        let balance = 0;

        transactionsList.innerHTML = '';
        transactions.forEach(t => {
            if (t.type === 'INCOME') {
                balance += t.amount;
            } else {
                balance -= t.amount;
            }

            const li = document.createElement('li');
            li.className = 'transaction-item';

            const amountClass = t.type === 'INCOME' ? 'income' : 'expense';
            const sign = t.type === 'INCOME' ? '+' : '-';

            // Safely construct DOM elements to prevent XSS

            const divInfo = document.createElement('div');
            divInfo.className = 't-info';

            const spanDesc = document.createElement('span');
            spanDesc.className = 't-desc';
            spanDesc.textContent = t.description; // Safe text

            const spanCat = document.createElement('span');
            spanCat.className = 't-cat';

            const spanDot = document.createElement('span');
            spanDot.className = 'cat-color-dot';
            spanDot.style.backgroundColor = t.category.color; // Safe assignment

            const textCatName = document.createTextNode(' ' + t.category.name); // Safe text

            spanCat.appendChild(spanDot);
            spanCat.appendChild(textCatName);

            const spanDate = document.createElement('span');
            spanDate.className = 't-date';
            spanDate.textContent = t.transactionDate;

            divInfo.appendChild(spanDesc);
            divInfo.appendChild(spanCat);
            divInfo.appendChild(spanDate);

            const divAmount = document.createElement('div');
            divAmount.className = `t-amount ${amountClass}`;
            divAmount.textContent = `${sign}$${t.amount.toFixed(2)}`;

            li.appendChild(divInfo);
            li.appendChild(divAmount);

            transactionsList.appendChild(li);
        });

        balanceAmount.textContent = `$${balance.toFixed(2)}`;
        if (balance < 0) {
            balanceAmount.style.color = '#ff6b6b';
        } else {
            balanceAmount.style.color = '#fff';
        }

    } catch (err) {
        console.error('Failed to load transactions', err);
    }
}

// Start
init();
