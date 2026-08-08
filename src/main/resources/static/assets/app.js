const state = {
    token: localStorage.getItem('autotrack_token'),
    user: null,
    vehicles: [],
    drivers: [],
    alerts: [],
    positions: new Map(),
    socket: null,
    reconnectTimer: null,
    intervals: []
};

const el = (id) => document.getElementById(id);

async function api(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (options.body && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json');
    }
    if (state.token) {
        headers.set('Authorization', `Bearer ${state.token}`);
    }

    const response = await fetch(path, { ...options, headers });
    if (response.status === 401) {
        logout(false);
        throw new Error('La sesion expiro. Inicia sesion nuevamente.');
    }
    if (!response.ok) {
        let message = `HTTP ${response.status}`;
        try {
            const body = await response.json();
            message = body.message || message;
            if (body.validationErrors && Object.keys(body.validationErrors).length) {
                message += ' ' + Object.entries(body.validationErrors)
                    .map(([field, error]) => `${field}: ${error}`)
                    .join(', ');
            }
        } catch (_) {
            // Keep generic message.
        }
        throw new Error(message);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, (char) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#039;',
        '"': '&quot;'
    })[char]);
}

function statusLabel(status) {
    const labels = {
        AVAILABLE: 'Disponible',
        MOVING: 'En movimiento',
        STOPPED: 'Detenido',
        MAINTENANCE: 'Mantenimiento',
        OUT_OF_SERVICE: 'Fuera de servicio'
    };
    return labels[status] || status || '-';
}

function formatDate(value) {
    if (!value) return 'Sin datos';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return 'Sin datos';
    return new Intl.DateTimeFormat('es-PE', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    }).format(date);
}

function showToast(message, isError = false) {
    const toast = el('toast');
    toast.textContent = message;
    toast.classList.remove('hidden', 'error');
    if (isError) toast.classList.add('error');
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => toast.classList.add('hidden'), 3500);
}

function setSocketStatus(online) {
    const pill = el('socketStatus');
    pill.classList.toggle('online', online);
    pill.classList.toggle('offline', !online);
    pill.querySelector('strong').textContent = online ? 'Tiempo real activo' : 'Desconectado';
}

function applyUserUi() {
    if (!state.user) return;
    el('userName').textContent = state.user.name;
    el('userRole').textContent = state.user.role;
    const initials = state.user.name
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join('') || 'AT';
    el('userInitials').textContent = initials;

    document.querySelectorAll('.form-panel').forEach((panel) => {
        panel.classList.toggle('hidden', state.user.role !== 'ADMIN');
    });
}

async function login(email, password) {
    const response = await api('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    });
    state.token = response.token;
    state.user = response.user;
    localStorage.setItem('autotrack_token', state.token);
    await startDashboard();
}

async function restoreSession() {
    if (!state.token) return;
    try {
        state.user = await api('/api/users/me');
        await startDashboard();
    } catch (error) {
        localStorage.removeItem('autotrack_token');
        state.token = null;
    }
}

async function startDashboard() {
    el('loginView').classList.add('hidden');
    el('dashboardView').classList.remove('hidden');
    applyUserUi();
    await refreshAll();
    connectSocket();
    startIntervals();
    requestAnimationFrame(drawTrackingBoard);
}

function logout(showMessage = true) {
    state.token = null;
    state.user = null;
    state.vehicles = [];
    state.drivers = [];
    state.alerts = [];
    state.positions.clear();
    localStorage.removeItem('autotrack_token');
    stopIntervals();
    if (state.reconnectTimer) window.clearTimeout(state.reconnectTimer);
    if (state.socket) {
        state.socket.onclose = null;
        state.socket.close();
        state.socket = null;
    }
    setSocketStatus(false);
    el('dashboardView').classList.add('hidden');
    el('loginView').classList.remove('hidden');
    if (showMessage) showToast('Sesion cerrada.');
}

async function refreshAll() {
    try {
        await Promise.all([loadVehicles(), loadDrivers(), loadMetrics(), loadAlerts()]);
    } catch (error) {
        showToast(error.message, true);
    }
}

async function loadVehicles() {
    state.vehicles = await api('/api/vehicles');
    state.vehicles.forEach((vehicle) => {
        if (vehicle.lastLatitude != null && vehicle.lastLongitude != null) {
            state.positions.set(vehicle.id, {
                vehicleId: vehicle.id,
                plate: vehicle.plate,
                latitude: vehicle.lastLatitude,
                longitude: vehicle.lastLongitude,
                speed: vehicle.lastSpeed || 0,
                heading: vehicle.lastHeading,
                recordedAt: vehicle.lastSeen
            });
        }
    });
    renderVehicleTable();
    renderVehicleCards();
    drawTrackingBoard();
}

async function loadDrivers() {
    state.drivers = await api('/api/drivers');
    renderDriverCards();
    renderDriverSelect();
}

async function loadMetrics() {
    const metrics = await api('/api/dashboard');
    el('metricTotal').textContent = metrics.totalVehicles;
    el('metricOnline').textContent = metrics.onlineVehicles;
    el('metricMoving').textContent = metrics.movingVehicles;
    el('metricAlerts').textContent = metrics.activeAlerts;
}

async function loadAlerts() {
    state.alerts = await api('/api/alerts');
    renderAlerts();
}

function renderVehicleTable() {
    const body = el('overviewVehiclesBody');
    if (!state.vehicles.length) {
        body.innerHTML = '<tr><td colspan="6">No hay vehiculos registrados.</td></tr>';
        return;
    }
    body.innerHTML = state.vehicles.map((vehicle) => `
        <tr>
            <td><strong>${escapeHtml(vehicle.plate)}</strong></td>
            <td>${escapeHtml(vehicle.brand)} ${escapeHtml(vehicle.model)} <span class="muted">(${vehicle.year})</span></td>
            <td><span class="status-badge status-${escapeHtml(vehicle.status)}">${escapeHtml(statusLabel(vehicle.status))}</span></td>
            <td>${vehicle.lastSpeed == null ? '-' : `${Number(vehicle.lastSpeed).toFixed(1)} km/h`}</td>
            <td>${vehicle.driver ? escapeHtml(vehicle.driver.fullName) : 'Sin asignar'}</td>
            <td>${escapeHtml(formatDate(vehicle.lastSeen))}</td>
        </tr>`).join('');
}

function renderVehicleCards() {
    const container = el('vehicleCards');
    if (!state.vehicles.length) {
        container.innerHTML = '<div class="empty-state">No hay vehiculos registrados.</div>';
        return;
    }
    const admin = state.user?.role === 'ADMIN';
    container.innerHTML = state.vehicles.map((vehicle) => `
        <div class="entity-card">
            <div class="entity-card-main">
                <strong>${escapeHtml(vehicle.plate)} - ${escapeHtml(vehicle.brand)} ${escapeHtml(vehicle.model)}</strong>
                <span>${escapeHtml(statusLabel(vehicle.status))} | Max ${Number(vehicle.maxSpeed || 0).toFixed(0)} km/h | ${vehicle.driver ? escapeHtml(vehicle.driver.fullName) : 'Sin conductor'}</span>
            </div>
            <div class="entity-actions">
                <span class="status-badge status-${escapeHtml(vehicle.status)}">${escapeHtml(statusLabel(vehicle.status))}</span>
                ${admin ? `<button class="btn btn-danger btn-small" data-delete-vehicle="${vehicle.id}">Eliminar</button>` : ''}
            </div>
        </div>`).join('');
}

function renderDriverCards() {
    const container = el('driverCards');
    if (!state.drivers.length) {
        container.innerHTML = '<div class="empty-state">No hay conductores registrados.</div>';
        return;
    }
    const admin = state.user?.role === 'ADMIN';
    container.innerHTML = state.drivers.map((driver) => `
        <div class="entity-card">
            <div class="entity-card-main">
                <strong>${escapeHtml(driver.fullName)}</strong>
                <span>Licencia ${escapeHtml(driver.licenseNumber)} | ${escapeHtml(driver.phone || 'Sin telefono')}</span>
            </div>
            <div class="entity-actions">
                <span class="status-badge ${driver.active ? 'status-MOVING' : 'status-OUT_OF_SERVICE'}">${driver.active ? 'Activo' : 'Inactivo'}</span>
                ${admin ? `<button class="btn btn-danger btn-small" data-delete-driver="${driver.id}">Eliminar</button>` : ''}
            </div>
        </div>`).join('');
}

function renderDriverSelect() {
    const select = el('vehicleDriver');
    const current = select.value;
    select.innerHTML = '<option value="">Sin asignar</option>' + state.drivers
        .filter((driver) => driver.active)
        .map((driver) => `<option value="${driver.id}">${escapeHtml(driver.fullName)} - ${escapeHtml(driver.licenseNumber)}</option>`)
        .join('');
    select.value = current;
}

function renderAlerts() {
    const container = el('alertList');
    if (!state.alerts.length) {
        container.innerHTML = '<div class="empty-state">No existen alertas.</div>';
        return;
    }
    const admin = state.user?.role === 'ADMIN';
    container.innerHTML = state.alerts.map((alert) => `
        <div class="alert-item ${alert.acknowledged ? 'acknowledged' : ''}">
            <div class="alert-icon">!</div>
            <div class="alert-body">
                <strong>${escapeHtml(alert.type)} - ${escapeHtml(alert.plate)}</strong>
                <span>${escapeHtml(alert.message)}</span>
                <small>${escapeHtml(formatDate(alert.createdAt))}</small>
            </div>
            <div class="entity-actions">
                ${!alert.acknowledged && admin ? `<button class="btn btn-secondary btn-small" data-ack-alert="${alert.id}">Reconocer</button>` : '<span class="status-badge">Revisada</span>'}
            </div>
        </div>`).join('');
}

function renderTelemetry(location) {
    const vehicle = state.vehicles.find((item) => item.id === location.vehicleId);
    el('lastTelemetry').innerHTML = `
        <div class="telemetry-card">
            <div class="telemetry-title">
                <strong>${escapeHtml(location.plate)}</strong>
                <span>${escapeHtml(formatDate(location.recordedAt))}</span>
            </div>
            <div class="telemetry-grid">
                <div><small>Velocidad</small><strong>${Number(location.speed).toFixed(1)} km/h</strong></div>
                <div><small>Rumbo</small><strong>${location.heading == null ? '-' : `${Number(location.heading).toFixed(0)} deg`}</strong></div>
                <div><small>Latitud</small><strong>${Number(location.latitude).toFixed(6)}</strong></div>
                <div><small>Longitud</small><strong>${Number(location.longitude).toFixed(6)}</strong></div>
            </div>
            ${vehicle ? `<span class="status-badge status-${escapeHtml(vehicle.status)}">${escapeHtml(statusLabel(vehicle.status))}</span>` : ''}
        </div>`;
}

function updateFromLocation(location) {
    state.positions.set(location.vehicleId, location);
    const vehicle = state.vehicles.find((item) => item.id === location.vehicleId);
    if (vehicle) {
        vehicle.lastLatitude = location.latitude;
        vehicle.lastLongitude = location.longitude;
        vehicle.lastSpeed = location.speed;
        vehicle.lastHeading = location.heading;
        vehicle.lastSeen = location.recordedAt;
        if (!['MAINTENANCE', 'OUT_OF_SERVICE'].includes(vehicle.status)) {
            vehicle.status = Number(location.speed) > 1 ? 'MOVING' : 'STOPPED';
        }
    }
    renderTelemetry(location);
    renderVehicleTable();
    renderVehicleCards();
    drawTrackingBoard();
}

function connectSocket() {
    if (!state.token) return;
    if (state.socket && [WebSocket.OPEN, WebSocket.CONNECTING].includes(state.socket.readyState)) return;

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const url = `${protocol}://${window.location.host}/ws/locations?token=${encodeURIComponent(state.token)}`;
    const socket = new WebSocket(url);
    state.socket = socket;

    socket.onopen = () => setSocketStatus(true);
    socket.onmessage = (event) => {
        try {
            const message = JSON.parse(event.data);
            if (message.type === 'vehicle.location' && message.data) {
                updateFromLocation(message.data);
            }
        } catch (error) {
            console.error('Invalid WebSocket event', error);
        }
    };
    socket.onerror = () => setSocketStatus(false);
    socket.onclose = () => {
        setSocketStatus(false);
        state.socket = null;
        if (state.token) {
            state.reconnectTimer = window.setTimeout(connectSocket, 3000);
        }
    };
}

function startIntervals() {
    stopIntervals();
    state.intervals.push(window.setInterval(() => loadMetrics().catch(() => {}), 5000));
    state.intervals.push(window.setInterval(() => loadAlerts().catch(() => {}), 8000));
    state.intervals.push(window.setInterval(() => loadVehicles().catch(() => {}), 15000));
}

function stopIntervals() {
    state.intervals.forEach((id) => window.clearInterval(id));
    state.intervals = [];
}

function drawTrackingBoard() {
    const canvas = el('trackingCanvas');
    if (!canvas || canvas.offsetParent === null) return;
    const rect = canvas.getBoundingClientRect();
    if (!rect.width || !rect.height) return;
    const dpr = window.devicePixelRatio || 1;
    canvas.width = Math.round(rect.width * dpr);
    canvas.height = Math.round(rect.height * dpr);
    const ctx = canvas.getContext('2d');
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    const width = rect.width;
    const height = rect.height;
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = '#07131b';
    ctx.fillRect(0, 0, width, height);

    ctx.strokeStyle = 'rgba(96,165,250,.09)';
    ctx.lineWidth = 1;
    for (let x = 0; x <= width; x += 45) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, height);
        ctx.stroke();
    }
    for (let y = 0; y <= height; y += 45) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(width, y);
        ctx.stroke();
    }

    const points = Array.from(state.positions.values())
        .filter((point) => Number.isFinite(Number(point.latitude)) && Number.isFinite(Number(point.longitude)));

    if (!points.length) {
        ctx.fillStyle = '#8ca3b3';
        ctx.font = '14px system-ui, sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('Esperando la primera posicion GPS...', width / 2, height / 2);
        return;
    }

    let minLat = Math.min(...points.map((p) => Number(p.latitude)));
    let maxLat = Math.max(...points.map((p) => Number(p.latitude)));
    let minLon = Math.min(...points.map((p) => Number(p.longitude)));
    let maxLon = Math.max(...points.map((p) => Number(p.longitude)));
    const minSpan = 0.004;
    if (maxLat - minLat < minSpan) {
        const mid = (maxLat + minLat) / 2;
        minLat = mid - minSpan / 2;
        maxLat = mid + minSpan / 2;
    }
    if (maxLon - minLon < minSpan) {
        const mid = (maxLon + minLon) / 2;
        minLon = mid - minSpan / 2;
        maxLon = mid + minSpan / 2;
    }

    const padding = 40;
    points.forEach((point) => {
        const x = padding + ((Number(point.longitude) - minLon) / (maxLon - minLon)) * (width - padding * 2);
        const y = height - padding - ((Number(point.latitude) - minLat) / (maxLat - minLat)) * (height - padding * 2);

        ctx.beginPath();
        ctx.arc(x, y, 16, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(54,211,153,.11)';
        ctx.fill();

        ctx.beginPath();
        ctx.arc(x, y, 6, 0, Math.PI * 2);
        ctx.fillStyle = '#36d399';
        ctx.fill();
        ctx.strokeStyle = '#b5f5dc';
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.fillStyle = '#eef6fb';
        ctx.font = '700 12px system-ui, sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(String(point.plate || `#${point.vehicleId}`), x, y - 24);
        ctx.fillStyle = '#8ca3b3';
        ctx.font = '11px ui-monospace, monospace';
        ctx.fillText(`${Number(point.speed || 0).toFixed(0)} km/h`, x, y + 29);
    });
}

function setupNavigation() {
    const titles = {
        overviewSection: 'Resumen de flota',
        vehiclesSection: 'Gestion de vehiculos',
        driversSection: 'Gestion de conductores',
        alertsSection: 'Centro de alertas'
    };
    document.querySelectorAll('.nav-item').forEach((button) => {
        button.addEventListener('click', () => {
            document.querySelectorAll('.nav-item').forEach((item) => item.classList.remove('active'));
            button.classList.add('active');
            document.querySelectorAll('.page-section').forEach((section) => section.classList.add('hidden'));
            const target = button.dataset.target;
            el(target).classList.remove('hidden');
            el('pageTitle').textContent = titles[target] || 'AutoTrack';
            if (target === 'overviewSection') requestAnimationFrame(drawTrackingBoard);
        });
    });
}

el('loginForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const button = event.currentTarget.querySelector('button[type="submit"]');
    el('loginError').textContent = '';
    button.disabled = true;
    try {
        await login(el('email').value, el('password').value);
    } catch (error) {
        el('loginError').textContent = error.message;
    } finally {
        button.disabled = false;
    }
});

el('logoutButton').addEventListener('click', () => logout(true));
el('refreshButton').addEventListener('click', refreshAll);

el('vehicleForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const driverValue = el('vehicleDriver').value;
    const payload = {
        plate: el('vehiclePlate').value,
        brand: el('vehicleBrand').value,
        model: el('vehicleModel').value,
        year: Number(el('vehicleYear').value),
        status: 'AVAILABLE',
        maxSpeed: Number(el('vehicleMaxSpeed').value),
        driverId: driverValue ? Number(driverValue) : null
    };
    try {
        await api('/api/vehicles', { method: 'POST', body: JSON.stringify(payload) });
        event.currentTarget.reset();
        el('vehicleYear').value = new Date().getFullYear();
        el('vehicleMaxSpeed').value = '80';
        await Promise.all([loadVehicles(), loadMetrics()]);
        showToast('Vehiculo registrado correctamente.');
    } catch (error) {
        showToast(error.message, true);
    }
});

el('driverForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const payload = {
        fullName: el('driverName').value,
        licenseNumber: el('driverLicense').value,
        phone: el('driverPhone').value || null,
        active: true
    };
    try {
        await api('/api/drivers', { method: 'POST', body: JSON.stringify(payload) });
        event.currentTarget.reset();
        await loadDrivers();
        showToast('Conductor registrado correctamente.');
    } catch (error) {
        showToast(error.message, true);
    }
});

el('vehicleCards').addEventListener('click', async (event) => {
    const button = event.target.closest('[data-delete-vehicle]');
    if (!button) return;
    const id = Number(button.dataset.deleteVehicle);
    if (!window.confirm('Eliminar este vehiculo y su historial de telemetria?')) return;
    try {
        await api(`/api/vehicles/${id}`, { method: 'DELETE' });
        state.positions.delete(id);
        await Promise.all([loadVehicles(), loadMetrics(), loadAlerts()]);
        showToast('Vehiculo eliminado.');
    } catch (error) {
        showToast(error.message, true);
    }
});

el('driverCards').addEventListener('click', async (event) => {
    const button = event.target.closest('[data-delete-driver]');
    if (!button) return;
    const id = Number(button.dataset.deleteDriver);
    if (!window.confirm('Eliminar este conductor?')) return;
    try {
        await api(`/api/drivers/${id}`, { method: 'DELETE' });
        await loadDrivers();
        showToast('Conductor eliminado.');
    } catch (error) {
        showToast(error.message, true);
    }
});

el('alertList').addEventListener('click', async (event) => {
    const button = event.target.closest('[data-ack-alert]');
    if (!button) return;
    try {
        await api(`/api/alerts/${button.dataset.ackAlert}/acknowledge`, { method: 'PATCH' });
        await Promise.all([loadAlerts(), loadMetrics()]);
        showToast('Alerta reconocida.');
    } catch (error) {
        showToast(error.message, true);
    }
});

window.addEventListener('resize', () => requestAnimationFrame(drawTrackingBoard));

setupNavigation();
restoreSession();
