const API_URL = '/api';

// State
let token = localStorage.getItem('token') || null;
let user = JSON.parse(localStorage.getItem('user')) || null;
let categories = [];
let transactions = [];
let cryptoDashboardData = null;
let currentCryptoCurrency = 'USD';

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
const addCryptoForm = document.getElementById('add-crypto-form');
const cryptoCurrencyToggle = document.getElementById('crypto-currency-toggle');

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

cryptoCurrencyToggle.addEventListener('change', (e) => {
    currentCryptoCurrency = e.target.value;
    renderCryptoDashboard();
});

addCryptoForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const coinId = document.getElementById('crypto-coin-id').value.trim().toLowerCase();
    const amount = parseFloat(document.getElementById('crypto-amount').value);
    const msgEl = document.getElementById('crypto-msg');
    msgEl.textContent = '';

    try {
        await apiCall('/crypto', 'POST', { coinId, amount });
        msgEl.textContent = 'Crypto asset updated!';
        msgEl.style.color = '#51cf66';
        addCryptoForm.reset();
        setTimeout(() => msgEl.textContent = '', 3000);
        await loadCryptoData();
    } catch (err) {
        msgEl.textContent = 'Error: ' + err.message;
        msgEl.style.color = '#ff6b6b';
    }
});

async function removeCryptoHolding(id) {
    try {
        await apiCall(`/crypto/${id}`, 'DELETE');
        await loadCryptoData();
    } catch (err) {
        alert('Failed to remove asset: ' + err.message);
    }
}


// Data Loading
async function loadDashboardData() {
    await loadCategories();
    await loadTransactions();
    await loadCryptoData();
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

async function loadCryptoData() {
    try {
        cryptoDashboardData = await apiCall('/crypto');
        renderCryptoDashboard();
    } catch (err) {
        console.error('Failed to load crypto data', err);
    }
}

function renderCryptoDashboard() {
    if (!cryptoDashboardData) return;

    const isUSD = currentCryptoCurrency === 'USD';
    const totalEl = document.getElementById('crypto-total-value');
    const tbody = document.getElementById('crypto-list');

    const prefix = isUSD ? '$' : 'R$';
    const totalValue = isUSD ? cryptoDashboardData.totalValueUsd : cryptoDashboardData.totalValueBrl;

    totalEl.textContent = `${prefix}${totalValue.toFixed(2)}`;

    tbody.innerHTML = '';

    cryptoDashboardData.assets.forEach(asset => {
        const price = isUSD ? asset.currentPriceUsd : asset.currentPriceBrl;
        const value = isUSD ? asset.totalValueUsd : asset.totalValueBrl;

        const tr = document.createElement('tr');

        const c24h = asset.change24h || 0;
        const c7d = asset.change7d || 0;
        const c30d = asset.change30d || 0;

        const cls24h = c24h >= 0 ? 'percent-positive' : 'percent-negative';
        const cls7d = c7d >= 0 ? 'percent-positive' : 'percent-negative';
        const cls30d = c30d >= 0 ? 'percent-positive' : 'percent-negative';

        const tdName = document.createElement('td');
        const strongName = document.createElement('strong');
        strongName.textContent = asset.name;
        tdName.appendChild(strongName);
        tdName.appendChild(document.createTextNode(` (${asset.symbol.toUpperCase()})`));

        const tdAmount = document.createElement('td');
        tdAmount.textContent = asset.amount;

        const tdPrice = document.createElement('td');
        tdPrice.textContent = `${prefix}${price.toFixed(2)}`;

        const tdValue = document.createElement('td');
        const strongValue = document.createElement('strong');
        strongValue.textContent = `${prefix}${value.toFixed(2)}`;
        tdValue.appendChild(strongValue);

        const td24h = document.createElement('td');
        td24h.className = cls24h;
        td24h.textContent = `${c24h.toFixed(2)}%`;

        const td7d = document.createElement('td');
        td7d.className = cls7d;
        td7d.textContent = `${c7d.toFixed(2)}%`;

        const td30d = document.createElement('td');
        td30d.className = cls30d;
        td30d.textContent = `${c30d.toFixed(2)}%`;

        const tdAction = document.createElement('td');
        const btnRemove = document.createElement('button');
        btnRemove.className = 'btn-remove';
        btnRemove.textContent = 'Remove';
        btnRemove.onclick = () => removeCryptoHolding(asset.holdingId);
        tdAction.appendChild(btnRemove);

        tr.appendChild(tdName);
        tr.appendChild(tdAmount);
        tr.appendChild(tdPrice);
        tr.appendChild(tdValue);
        tr.appendChild(td24h);
        tr.appendChild(td7d);
        tr.appendChild(td30d);
        tr.appendChild(tdAction);

        tbody.appendChild(tr);
    });
}

// Start
init();
