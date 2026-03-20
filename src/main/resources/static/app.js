const API_BASE = 'http://localhost:8084';
const TOKEN_KEY = 'insureai_token';

const state = {
    currentSection: 'dashboard',
    charts: {
        statusChart: null,
        overviewChart: null,
        utilizationChart: null
    },
    lastMatch: null
};

// JWT Token management
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

function removeToken() {
    localStorage.removeItem(TOKEN_KEY);
}

function isLoggedIn() {
    return !!getToken();
}

// Check auth on load
function checkAuth() {
    if (!isLoggedIn()) {
        const currentPage = window.location.pathname.split('/').pop();
        if (!['login.html', 'index.html', ''].includes(currentPage)) {
            window.location.href = 'login.html';
        }
    }
}

// Global unhandled promise rejection handler (helps surface silent failures)
window.addEventListener('unhandledrejection', event => {
    console.error('Unhandled promise rejection:', event.reason);
    showError(event.reason?.message || String(event.reason) || 'Unexpected error occurred');
});

function qs(selector) {
    return document.querySelector(selector);
}

function qsa(selector) {
    return Array.from(document.querySelectorAll(selector));
}

function showToast(message, type = 'info', duration = 3200) {
    const colors = {
        info: 'bg-blue-500',
        success: 'bg-emerald-500',
        warning: 'bg-amber-500',
        error: 'bg-rose-500'
    };

    const toast = document.createElement('div');
    toast.className = `toast fixed right-4 bottom-6 z-50 max-w-xs rounded-xl px-4 py-3 text-sm text-white shadow-lg ${colors[type] || colors.info}`;
    toast.textContent = message;

    document.body.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('opacity-0', 'transition', 'duration-500');
        setTimeout(() => toast.remove(), 500);
    }, duration);
}

function showSuccess(message) {
    showToast(message, 'success');
}

function showError(message) {
    showToast(message, 'error');
}

function showLoading(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = '<div class="flex justify-center items-center py-8"><div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div></div>';
    }
}

function setActiveNav(section) {
    qsa('.nav-btn').forEach(btn => {
        btn.classList.toggle('bg-indigo-500/20', btn.dataset.section === section);
        btn.classList.toggle('text-white', btn.dataset.section === section);
        btn.classList.toggle('text-slate-300', btn.dataset.section !== section);
    });
}

function showSection(section) {
    if (!qs('#dashboard')) return; // Only run on pages with sections
    state.currentSection = section;
    qsa('.section').forEach(sec => sec.classList.add('hidden'));
    const selected = qs(`#${section}`);
    if (selected) {
        selected.classList.remove('hidden');
    }
    setActiveNav(section);
}

function toggleMobileNav() {
    const sidebar = qs('#sidebar');
    if (!sidebar) return;
    sidebar.classList.toggle('hidden');
    sidebar.classList.toggle('absolute');
    sidebar.classList.toggle('z-50');
    sidebar.classList.toggle('h-full');
    sidebar.classList.toggle('md:block');
    sidebar.classList.toggle('w-72');
    sidebar.classList.toggle('bg-slate-950/90');
}
async function apiRequest(url, options = {}) {

    const token = getToken();

    const headers = {
        "Content-Type": "application/json",
        ...(token && { "Authorization": `Bearer ${token}` })
    };

    const config = {
        ...options,
        headers: { ...headers, ...(options.headers || {}) }
    };

    const res = await fetch(`${API_BASE}${url}`, config);

    if (res.status === 401) {
        removeToken();
        window.location.href = "login.html";
        return;
    }

    if (res.status === 204) {
        return null;
    }

    if (!res.ok) {
        const contentType = res.headers.get('content-type') || '';
        const text = await res.text();
        let message = text;
        if (contentType.includes('application/json')) {
            try {
                const json = JSON.parse(text);
                message = json.error || json.message || JSON.stringify(json);
            } catch (e) {
                // ignore parsing errors
            }
        }
        throw new Error(message || `Request failed (${res.status})`);
    }

    const contentType = res.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return res.json();
    }

    return res.text();

}

async function login(email, password) {
    const response = await apiRequest('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    });

    if (response && response.token) {
        setToken(response.token);
    }

    return response;
}

async function register(name, password, email, role) {

    return await apiRequest('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({
            name: name,
            email: email,
            password: password,
            role: role
        })
    });

}

window.register = register; // Expose register globally for login.html to call

function logout() {
    removeToken();
    window.location.href = 'login.html';
}

async function loadStats() {

    if (!qs('#bookedCount')) return;

    try {

        const data = await apiRequest('/api/report/stats');

        const booked =
            Number(data.BOOKED ?? data.booked ?? 0);

        const completed =
            Number(data.COMPLETED ?? data.completed ?? 0);

        const cancelled =
            Number(data.CANCELLED ?? data.cancelled ?? 0);

        const agentUtilization =
            data.agentUtilization ?? {};

        qs('#bookedCount').textContent = booked;
        qs('#completedCount').textContent = completed;
        qs('#cancelledCount').textContent = cancelled;

        // Also update auto booking stats if they exist
        if (qs('#autoBookedCount')) qs('#autoBookedCount').textContent = booked;
        if (qs('#autoCompletedCount')) qs('#autoCompletedCount').textContent = completed;
        if (qs('#autoCancelledCount')) qs('#autoCancelledCount').textContent = cancelled;

        updateCharts({
            booked,
            completed,
            cancelled,
            agentUtilization
        });

    } catch (err) {

        console.error("Stats error:", err);
        showToast('Unable to load stats', 'error');

    }

}

// Expose global helper for pages that override loadStats
window.__insureai_loadStats = loadStats;

function initCharts() {
    if (!qs('#statusChart')) return; // Only run on pages with charts
    const statusCtx = qs('#statusChart');
    const overviewCtx = qs('#overviewChart');

    if (statusCtx) {
        state.charts.statusChart = new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: ['Booked', 'Completed', 'Cancelled'],
                datasets: [{
                    data: [0, 0, 0],
                    backgroundColor: ['#6366F1', '#22C55E', '#EF4444'],
                    borderWidth: 0
                }]
            },
            options: {
                plugins: {
                    legend: { position: 'bottom', labels: { color: '#CBD5E1' } },
                    tooltip: { callbacks: { label: ctx => `${ctx.label}: ${ctx.parsed}` } }
                }
            }
        });
    }

    if (overviewCtx) {
        state.charts.overviewChart = new Chart(overviewCtx, {
            type: 'bar',
            data: {
                labels: ['Booked', 'Completed', 'Cancelled'],
                datasets: [{
                    label: 'Appointments',
                    data: [0, 0, 0],
                    backgroundColor: ['#6366F1', '#22C55E', '#EF4444'],
                    borderRadius: 8,
                    borderSkipped: false
                }]
            },
            options: {
                scales: {
                    x: { ticks: { color: '#CBD5E1' }, grid: { display: false } },
                    y: { ticks: { color: '#CBD5E1' }, grid: { color: 'rgba(148,163,184,0.2)' }, beginAtZero: true }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }

    const utilCtx = qs('#utilizationChart');
    if (utilCtx) {
        state.charts.utilizationChart = new Chart(utilCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Appointments per agent',
                    data: [],
                    backgroundColor: '#38BDF8',
                    borderRadius: 8,
                    borderSkipped: false
                }]
            },
            options: {
                indexAxis: 'y',
                scales: {
                    x: { ticks: { color: '#CBD5E1' }, grid: { color: 'rgba(148,163,184,0.2)' }, beginAtZero: true },
                    y: { ticks: { color: '#CBD5E1' }, grid: { display: false } }
                },
                plugins: {
                    legend: { display: false },
                    tooltip: { callbacks: { label: ctx => `${ctx.parsed.x} appointments` } }
                }
            }
        });
    }
}

function updateCharts({ booked, completed, cancelled, agentUtilization }) {
    if (state.charts.statusChart) {
        state.charts.statusChart.data.datasets[0].data = [booked, completed, cancelled];
        state.charts.statusChart.update();
    }

    if (state.charts.overviewChart) {
        state.charts.overviewChart.data.datasets[0].data = [booked, completed, cancelled];
        state.charts.overviewChart.update();
    }

    if (state.charts.utilizationChart && agentUtilization) {
        const agentIds = Object.keys(agentUtilization).sort((a, b) => Number(b) - Number(a));
        const counts = agentIds.map(id => agentUtilization[id]);
        state.charts.utilizationChart.data.labels = agentIds.map(id => `Agent ${id}`);
        state.charts.utilizationChart.data.datasets[0].data = counts;
        state.charts.utilizationChart.update();
    }
}

async function autoBook(customerId, expertise, location) {
    const url = `/api/auto/book?customerId=${encodeURIComponent(customerId)}&expertise=${encodeURIComponent(expertise)}&location=${encodeURIComponent(location)}`;
    const response = await apiRequest(url, {
        method: 'POST'
    });
    return response;
}

async function bookAppointment({ agentId, date, timeSlot }) {
    const payload = {
        agentId: Number(agentId),
        date,
        timeSlot
    };

    const response = await apiRequest('/api/appointments/book', {
        method: 'POST',
        body: JSON.stringify(payload)
    });

    return response;
}

async function handleBooking() {
    const agentId = qs('#agentId').value.trim();
    const date = qs('#appointmentDate').value;
    const timeSlot = qs('#timeSlot').value;

    if (!agentId || !date || !timeSlot) {
        showToast('Please complete all required booking fields', 'warning');
        return;
    }

    try {
        const result = await bookAppointment({ agentId, date, timeSlot });
        qs('#bookingResult').textContent = `Booked appointment with agent ${result.agentId}.`;
        qs('#bookingResult').classList.remove('hidden');
        await loadHistory();
        await loadStats();
        showToast('Appointment booked successfully', 'success');
    } catch (error) {
        console.error(error);
        showToast('Failed to book appointment', 'error');
    }
}

async function smartMatch() {
    const expertise = qs('#smExpertise').value.trim();
    const location = qs('#smLocation').value.trim();

    if (!expertise || !location) {
        showToast('Please enter expertise and location', 'warning');
        return;
    }

    try {
        const data = await apiRequest(`/api/availability/match?expertise=${encodeURIComponent(expertise)}&location=${encodeURIComponent(location)}`);
        state.lastMatch = data;
        qs('#matchAgentId').textContent = data.agentId ?? '–';
        qs('#matchScore').textContent = data.score ?? '–';
        qs('#matchExpertise').textContent = data.expertise || expertise;
        qs('#matchResultCard').classList.remove('hidden');
        showToast('Agent match completed', 'success');
        showSection('match');
    } catch (err) {
        console.error(err);
        showToast('Agent match failed', 'error');
    }
}

async function handleBookMatchedAgent() {
    if (!state.lastMatch || !state.lastMatch.agentId) {
        showToast('No match available. Find an agent first.', 'warning');
        return;
    }

    const date = qs('#appointmentDate').value;
    const timeSlot = qs('#timeSlot').value;

    if (!date || !timeSlot) {
        showToast('Enter date and time to book.', 'warning');
        showSection('booking');
        return;
    }

    try {
        const result = await bookAppointment({
            agentId: state.lastMatch.agentId,
            date,
            timeSlot
        });
        showToast('Booked with matched agent', 'success');
        qs('#bookingResult').textContent = `Booked with Agent ${result.agentId}.`;
        qs('#bookingResult').classList.remove('hidden');
        await loadHistory();
        await loadStats();
    } catch (err) {
        console.error(err);
        showToast('Booking failed', 'error');
    }
}

async function handleAutoBook() {
    const customerId = qs('#autoCustomerId').value.trim();
    const expertise = qs('#autoExpertise').value.trim();
    const location = qs('#autoLocation').value.trim();

    if (!customerId || !expertise || !location) {
        showToast('Please complete all auto booking fields', 'warning');
        return;
    }

    try {
        const result = await autoBook(customerId, expertise, location);
        showToast('Auto booking completed successfully', 'success');
        await loadHistory();
        await loadStats();
    } catch (error) {
        console.error(error);
        showToast('Auto booking failed', 'error');
    }
}

async function loadHistory() {
    if (!qs('#historyTable')) return; // Only run on pages with history table
    try {
        const appointments = await apiRequest('/api/appointments');
        const tbody = qs('#historyTable');
        const empty = qs('#historyEmpty');

        if (!appointments || !appointments.length) {
            tbody.innerHTML = '';
            empty.classList.remove('hidden');
            return;
        }

        empty.classList.add('hidden');

        const rows = appointments.map(a => {
            const disabled = a.status?.toUpperCase() === 'CANCELLED';
            return `
                <tr class="hover:bg-white/5">
                    <td class="px-4 py-3 text-sm font-medium text-slate-100">${a.agentId ?? '–'}</td>
                    <td class="px-4 py-3 text-sm text-slate-300">${a.date ?? '–'}</td>
                    <td class="px-4 py-3 text-sm text-slate-300">${a.timeSlot ?? '–'}</td>
                    <td class="px-4 py-3 text-sm">
                        <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ${getStatusClasses(a.status)}">${a.status ?? 'UNKNOWN'}</span>
                    </td>
                    <td class="px-4 py-3 text-sm">
                        <button class="cancel-btn rounded-xl px-4 py-2 text-xs font-semibold text-white ${disabled ? 'bg-slate-500 cursor-not-allowed' : 'bg-rose-500 hover:bg-rose-400'}" data-id="${a.id}" ${disabled ? 'disabled' : ''}>
                            Cancel
                        </button>
                    </td>
                </tr>
            `;
        }).join('');

        tbody.innerHTML = rows;

        qsa('.cancel-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                await cancelAppointment(id);
            });
        });
    } catch (err) {
        console.error(err);
        showToast('Unable to load appointment history', 'error');
    }
}

function getStatusClasses(status) {
    const s = (status || '').toString().toUpperCase();
    if (s === 'COMPLETED') return 'bg-emerald-500/20 text-emerald-100';
    if (s === 'CANCELLED') return 'bg-rose-500/20 text-rose-100';
    if (s === 'BOOKED') return 'bg-indigo-500/20 text-indigo-100';
    return 'bg-white/10 text-slate-100';
}

async function cancelAppointment(appointmentId) {
    if (!appointmentId) return;

    try {
        await apiRequest(`/api/appointments/updateStatus?appointmentId=${encodeURIComponent(appointmentId)}&status=CANCELLED`, {
            method: 'PUT'
        });

        showToast('Appointment cancelled', 'success');
        await loadHistory();
        await loadStats();
    } catch (err) {
        console.error(err);
        showToast('Failed to cancel appointment', 'error');
    }
}

async function completeAppointment(appointmentId) {
    if (!appointmentId) return;

    try {
        await apiRequest(`/api/appointments/updateStatus?appointmentId=${encodeURIComponent(appointmentId)}&status=COMPLETED`, {
            method: 'PUT'
        });

        showToast('Appointment completed', 'success');
        await loadHistory();
        await loadStats();
    } catch (err) {
        console.error(err);
        showToast('Failed to complete appointment', 'error');
    }
}

async function recommendPolicy() {
    const age = qs('#age').value.trim();
    const income = qs('#income').value.trim();
    const riskLevel = qs('#riskLevel')?.value;

    if (!age || !income || !riskLevel) {
        showToast('Complete all fields to get a recommendation', 'warning');
        return;
    }

   try {

    const data = await apiRequest(
        `/api/policy/recommend?age=${encodeURIComponent(age)}&income=${encodeURIComponent(income)}&riskLevel=${encodeURIComponent(riskLevel)}`
    );

    const policyType = qs('#policyType');
    if (policyType) {
        policyType.textContent = data.policyType || '–';
    }

    const policyRec = qs('#policyRec');
    if (policyRec) {
        policyRec.textContent = data.recommendation || 'No recommendation returned';
    }

    const policyResult = qs('#policyResult');
    if (policyResult) {
        policyResult.classList.remove('hidden');
    }

    showToast('Policy recommendation generated', 'success');

} catch (err) {

    console.error(err);
    showToast('Policy recommendation failed', 'error');

}
}
function bindActions() {
    qsa('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const section = btn.dataset.section;
            showSection(section);
        });
    });

    qs('#mobileNavToggle')?.addEventListener('click', () => {
        toggleMobileNav();
    });

    qs('#mobileRefresh')?.addEventListener('click', () => {
        loadStats();
    });

    qs('#refreshStats')?.addEventListener('click', () => {
        loadStats();
    });

    qs('#refreshHistory')?.addEventListener('click', () => {
        loadHistory();
    });

    qs('#bookAppointment')?.addEventListener('click', handleBooking);
    qs('#findAgent')?.addEventListener('click', smartMatch);
    qs('#bookMatchedAgent')?.addEventListener('click', handleBookMatchedAgent);
    qs('#autoBook')?.addEventListener('click', handleAutoBook);
    qs('#recommendPolicy')?.addEventListener('click', recommendPolicy);

    qs('#logoutBtn')?.addEventListener('click', logout);

    // Close mobile sidebar when selecting an item
    qsa('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const sidebar = qs('#sidebar');
            if (sidebar && window.innerWidth < 768) {
                sidebar.classList.add('hidden');
            }
        });
    });
}

function initialize() {
    checkAuth();
    initCharts();
    bindActions();
    showSection('dashboard');
    loadStats();
    loadHistory();
}

window.addEventListener('DOMContentLoaded', initialize);
