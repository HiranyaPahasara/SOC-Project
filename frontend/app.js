const TEMP_API = "/api/temperatures";
const CURRENCY_API = "/api/currency";
const TEMP_API_KEY = localStorage.getItem("TEMP_API_KEY") || "lab05-demo-key";

function tempHeaders(extra = {}) {
    return { "X-API-KEY": TEMP_API_KEY, ...extra };
}

document.addEventListener("DOMContentLoaded", () => {
    setupTabs();
    setupSegmentedControls();
    setupPresetChips();
    setupTemperatureForm();
    setupCurrencyForm();
    setupRefresh();
    setupLatestButtons();
    setupClearButtons();
    setupLivePreview();
    pingBackends();
    setInterval(pingBackends, 8000);
});

/* ============== Tabs with sliding pill ============== */
function setupTabs() {
    const tabs = document.querySelectorAll(".tab");
    const panels = document.querySelectorAll(".panel");
    const indicator = document.querySelector(".tab-indicator");

    const moveIndicator = (tab) => {
        if (!indicator || !tab) return;
        const rect = tab.getBoundingClientRect();
        const parentRect = tab.parentElement.getBoundingClientRect();
        indicator.style.left = rect.left - parentRect.left + "px";
        indicator.style.width = rect.width + "px";
    };

    const activeTab = document.querySelector(".tab.active");
    requestAnimationFrame(() => moveIndicator(activeTab));
    window.addEventListener("resize", () =>
        moveIndicator(document.querySelector(".tab.active"))
    );

    tabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            const target = tab.dataset.tab;
            tabs.forEach((t) => t.classList.remove("active"));
            tab.classList.add("active");
            moveIndicator(tab);

            panels.forEach((p) => p.classList.remove("active"));
            document.getElementById(`panel-${target}`).classList.add("active");

            if (target === "history") loadHistory();
        });
    });
}

/* ============== Segmented controls ============== */
function setupSegmentedControls() {
    document.querySelectorAll(".segmented").forEach((group) => {
        const targetId = group.dataset.target;
        const hidden = document.getElementById(targetId);
        const segs = group.querySelectorAll(".seg");

        segs.forEach((btn, idx) => {
            btn.addEventListener("click", () => {
                segs.forEach((b) => b.classList.remove("active"));
                btn.classList.add("active");
                hidden.value = btn.dataset.value;
                if (idx === 0) group.classList.remove("right");
                else group.classList.add("right");
                updateAffix();
                updateLivePreviews();
            });
        });
    });
}

function setupPresetChips() {
    document.querySelectorAll(".chip").forEach((chip) => {
        chip.addEventListener("click", () => {
            const target = document.getElementById(chip.dataset.target);
            target.value = chip.dataset.value;
            target.focus();
            updateLivePreviews();
        });
    });
}

function updateAffix() {
    const tempUnit = document.getElementById("temp-unit").value;
    const tempSuffix = document.getElementById("temp-suffix");
    if (tempSuffix) tempSuffix.textContent = tempUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";

    const currFrom = document.getElementById("currency-from").value;
    const currPrefix = document.getElementById("currency-prefix");
    if (currPrefix) currPrefix.textContent = currFrom === "USD" ? "$" : "Rs";
}

/* ============== Live preview ============== */
function setupLivePreview() {
    ["temp-value", "temp-unit", "currency-amount", "currency-from"].forEach((id) => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener("input", updateLivePreviews);
            el.addEventListener("change", updateLivePreviews);
        }
    });
}

let tempPreviewTimer = null;
let currencyPreviewTimer = null;
let tempPreviewSeq = 0;
let currencyPreviewSeq = 0;

function updateLivePreviews() {
    schedulePreview("temp");
    schedulePreview("currency");
}

function schedulePreview(kind) {
    if (kind === "temp") {
        clearTimeout(tempPreviewTimer);
        tempPreviewTimer = setTimeout(runTempPreview, 220);
    } else {
        clearTimeout(currencyPreviewTimer);
        currencyPreviewTimer = setTimeout(runCurrencyPreview, 220);
    }
}

async function runTempPreview() {
    const tp = document.getElementById("temp-preview");
    const tv = parseFloat(document.getElementById("temp-value").value);
    const tu = document.getElementById("temp-unit").value;

    if (isNaN(tv)) {
        tp.classList.remove("visible");
        return;
    }

    const seq = ++tempPreviewSeq;
    try {
        const url = `${TEMP_API}/preview?value=${encodeURIComponent(tv)}&unit=${encodeURIComponent(tu)}`;
        const res = await fetch(url, { headers: tempHeaders() });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (seq !== tempPreviewSeq) return;

        const inSym = data.inputUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";
        const outSym = data.outputUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";
        tp.innerHTML = `Preview: ${formatNumber(data.inputTemperature)}${inSym} &rarr; <span class="preview-result">${formatNumber(data.outputTemperature)}${outSym}</span>`;
        tp.classList.add("visible");
    } catch {
        if (seq !== tempPreviewSeq) return;
        tp.innerHTML = `Preview unavailable (backend offline)`;
        tp.classList.add("visible");
    }
}

async function runCurrencyPreview() {
    const cp = document.getElementById("currency-preview");
    const ca = parseFloat(document.getElementById("currency-amount").value);
    const cf = document.getElementById("currency-from").value;

    if (isNaN(ca)) {
        cp.classList.remove("visible");
        return;
    }

    const seq = ++currencyPreviewSeq;
    try {
        const url = `${CURRENCY_API}/preview?amount=${encodeURIComponent(ca)}&from=${encodeURIComponent(cf)}`;
        const res = await fetch(url);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (seq !== currencyPreviewSeq) return;

        cp.innerHTML = `Preview: ${formatNumber(data.inputAmount)} ${data.fromCurrency.toUpperCase()} &rarr; <span class="preview-result">${formatNumber(data.outputAmount)} ${data.toCurrency.toUpperCase()}</span>`;
        cp.classList.add("visible");
    } catch {
        if (seq !== currencyPreviewSeq) return;
        cp.innerHTML = `Preview unavailable (backend offline)`;
        cp.classList.add("visible");
    }
}

/* ============== Forms ============== */
function setupTemperatureForm() {
    const form = document.getElementById("temp-form");
    const resultEl = document.getElementById("temp-result");
    const errorEl = document.getElementById("temp-error");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        hide(resultEl);
        hide(errorEl);

        const value = document.getElementById("temp-value").value;
        const unit = document.getElementById("temp-unit").value;
        const btn = form.querySelector("button[type=submit]");
        setLoading(btn, true);

        try {
            const url = `${TEMP_API}/convert?value=${encodeURIComponent(value)}&unit=${encodeURIComponent(unit)}`;
            const res = await fetch(url, { method: "POST", headers: tempHeaders() });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            const data = await res.json();
            const inSym = data.inputUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";
            const outSym = data.outputUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";

            renderResult(resultEl, {
                fromValue: `${formatNumber(data.inputTemperature)}${inSym}`,
                toValue: data.outputTemperature,
                toSuffix: outSym,
            });
        } catch (err) {
            errorEl.textContent = `Could not reach Temperature backend. ${err.message}`;
            show(errorEl);
        } finally {
            setLoading(btn, false);
        }
    });
}

function setupCurrencyForm() {
    const form = document.getElementById("currency-form");
    const resultEl = document.getElementById("currency-result");
    const errorEl = document.getElementById("currency-error");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        hide(resultEl);
        hide(errorEl);

        const amount = document.getElementById("currency-amount").value;
        const from = document.getElementById("currency-from").value;
        const btn = form.querySelector("button[type=submit]");
        setLoading(btn, true);

        try {
            const url = `${CURRENCY_API}/convert?amount=${encodeURIComponent(amount)}&from=${encodeURIComponent(from)}`;
            const res = await fetch(url, { method: "POST" });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            const data = await res.json();
            renderResult(resultEl, {
                fromValue: `${formatNumber(data.inputAmount)} ${data.fromCurrency.toUpperCase()}`,
                toValue: data.outputAmount,
                toSuffix: ` ${data.toCurrency.toUpperCase()}`,
            });
        } catch (err) {
            errorEl.textContent = `Could not reach Currency backend. ${err.message}`;
            show(errorEl);
        } finally {
            setLoading(btn, false);
        }
    });
}

function renderResult(el, { fromValue, toValue, toSuffix }) {
    el.innerHTML = `
        <div class="row">
            <div class="side from-side">
                <span class="label">From</span>
                <span class="value">${fromValue}</span>
            </div>
            <svg class="arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M5 12h14M13 5l7 7-7 7"/>
            </svg>
            <div class="side to-side">
                <span class="label">Result</span>
                <span class="value">${formatNumber(Number(toValue))}${toSuffix}</span>
            </div>
        </div>
    `;
    show(el);
}

/* ============== Latest & Clear buttons ============== */
function setupLatestButtons() {
    const tempBtn = document.getElementById("temp-latest");
    if (tempBtn) tempBtn.addEventListener("click", () => loadLatest("temp"));

    const currBtn = document.getElementById("currency-latest");
    if (currBtn) currBtn.addEventListener("click", () => loadLatest("currency"));
}

async function loadLatest(kind) {
    const isTemp = kind === "temp";
    const url = (isTemp ? TEMP_API : CURRENCY_API) + "/latest";
    const resultEl = document.getElementById(isTemp ? "temp-result" : "currency-result");
    const errorEl = document.getElementById(isTemp ? "temp-error" : "currency-error");
    const btn = document.getElementById(isTemp ? "temp-latest" : "currency-latest");

    hide(resultEl);
    hide(errorEl);
    setLoading(btn, true);

    try {
        const res = await fetch(url, isTemp ? { headers: tempHeaders() } : undefined);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const text = await res.text();
        if (!text) {
            errorEl.textContent = "No conversions saved yet.";
            show(errorEl);
            return;
        }
        const data = JSON.parse(text);
        if (!data) {
            errorEl.textContent = "No conversions saved yet.";
            show(errorEl);
            return;
        }

        if (isTemp) {
            const inSym = data.inputUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";
            const outSym = data.outputUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";
            renderResult(resultEl, {
                fromValue: `${formatNumber(data.inputTemperature)}${inSym}`,
                toValue: data.outputTemperature,
                toSuffix: outSym,
            });
        } else {
            renderResult(resultEl, {
                fromValue: `${formatNumber(data.inputAmount)} ${data.fromCurrency.toUpperCase()}`,
                toValue: data.outputAmount,
                toSuffix: ` ${data.toCurrency.toUpperCase()}`,
            });
        }
    } catch (err) {
        errorEl.textContent = `Could not load latest. ${err.message}`;
        show(errorEl);
    } finally {
        setLoading(btn, false);
    }
}

function setupClearButtons() {
    const tempBtn = document.getElementById("clear-temp");
    if (tempBtn) tempBtn.addEventListener("click", () => clearHistory("temp"));

    const currBtn = document.getElementById("clear-currency");
    if (currBtn) currBtn.addEventListener("click", () => clearHistory("currency"));
}

async function clearHistory(kind) {
    const isTemp = kind === "temp";
    const label = isTemp ? "temperature" : "currency";
    if (!confirm(`Delete all saved ${label} conversions? This cannot be undone.`)) return;

    const url = (isTemp ? TEMP_API : CURRENCY_API) + "/clear";
    const btn = document.getElementById(isTemp ? "clear-temp" : "clear-currency");
    setLoading(btn, true);

    try {
        const res = await fetch(url, {
            method: "DELETE",
            ...(isTemp ? { headers: tempHeaders() } : {}),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        if (isTemp) loadTempHistory();
        else loadCurrencyHistory();
    } catch (err) {
        alert(`Could not clear ${label} history. ${err.message}`);
    } finally {
        setLoading(btn, false);
    }
}

/* ============== History ============== */
function setupRefresh() {
    document.getElementById("refresh-history").addEventListener("click", loadHistory);
}

async function loadHistory() {
    loadTempHistory();
    loadCurrencyHistory();
}

async function loadTempHistory() {
    const container = document.getElementById("temp-history");
    const countEl = document.getElementById("temp-count");
    container.innerHTML = `<p class="empty">Loading...</p>`;

    try {
        const res = await fetch(`${TEMP_API}/history`, { headers: tempHeaders() });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        countEl.textContent = data.length;
        renderTempHistory(container, data);
    } catch {
        container.innerHTML = `<p class="empty">Backend offline.</p>`;
        countEl.textContent = "—";
    }
}

async function loadCurrencyHistory() {
    const container = document.getElementById("currency-history");
    const countEl = document.getElementById("currency-count");
    container.innerHTML = `<p class="empty">Loading...</p>`;

    try {
        const res = await fetch(`${CURRENCY_API}/history`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        countEl.textContent = data.length;
        renderCurrencyHistory(container, data);
    } catch {
        container.innerHTML = `<p class="empty">Backend offline.</p>`;
        countEl.textContent = "—";
    }
}

function renderTempHistory(container, items) {
    if (!items || items.length === 0) {
        container.innerHTML = `<p class="empty">No conversions yet.</p>`;
        return;
    }
    const reversed = [...items].reverse();
    container.innerHTML = reversed
        .map((it, idx) => {
            const inSym = it.inputUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";
            const outSym = it.outputUnit === "CELSIUS" ? "\u00B0C" : "\u00B0F";
            return `
                <div class="history-item">
                    <div class="conv">
                        <span>${formatNumber(it.inputTemperature)}${inSym}</span>
                        <span class="mid-arrow">&rarr;</span>
                        <span>${formatNumber(it.outputTemperature)}${outSym}</span>
                    </div>
                    <span class="time">${formatTime(it.timestamp)}</span>
                </div>`;
        })
        .join("");
}

function renderCurrencyHistory(container, items) {
    if (!items || items.length === 0) {
        container.innerHTML = `<p class="empty">No conversions yet.</p>`;
        return;
    }
    const reversed = [...items].reverse();
    container.innerHTML = reversed
        .map(
            (it) => `
                <div class="history-item">
                    <div class="conv">
                        <span>${formatNumber(it.inputAmount)} ${it.fromCurrency.toUpperCase()}</span>
                        <span class="mid-arrow">&rarr;</span>
                        <span>${formatNumber(it.outputAmount)} ${it.toCurrency.toUpperCase()}</span>
                    </div>
                    <span class="time">${formatTime(it.timestamp)}</span>
                </div>`
        )
        .join("");
}

/* ============== Health checks ============== */
async function pingBackends() {
    pingOne(`${TEMP_API}/history`, "status-temp", tempHeaders());
    pingOne(`${CURRENCY_API}/history`, "status-currency");
}

async function pingOne(url, dotId, headers) {
    const dot = document.getElementById(dotId);
    try {
        const res = await fetch(url, { method: "GET", headers });
        dot.classList.remove("online", "offline");
        dot.classList.add(res.ok ? "online" : "offline");
    } catch {
        dot.classList.remove("online", "offline");
        dot.classList.add("offline");
    }
}

/* ============== Helpers ============== */
function setLoading(btn, loading) {
    if (loading) {
        btn.disabled = true;
        btn.dataset.original = btn.innerHTML;
        btn.innerHTML = `<span>Converting...</span>`;
    } else {
        btn.disabled = false;
        if (btn.dataset.original) {
            btn.innerHTML = btn.dataset.original;
            delete btn.dataset.original;
        }
    }
}

function formatNumber(n) {
    if (n === undefined || n === null || isNaN(n)) return "—";
    const num = Number(n);
    if (Number.isInteger(num)) return num.toLocaleString();
    return num.toLocaleString(undefined, { maximumFractionDigits: 2 });
}

function formatTime(ts) {
    if (!ts) return "";
    try {
        const d = new Date(ts);
        if (Number.isNaN(d.getTime())) return ts;
        const diff = (Date.now() - d.getTime()) / 1000;
        if (diff < 60) return "just now";
        if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
        if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
        return d.toLocaleString(undefined, {
            month: "short",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    } catch {
        return ts;
    }
}

function show(el) { el.classList.remove("hidden"); }
function hide(el) { el.classList.add("hidden"); }
