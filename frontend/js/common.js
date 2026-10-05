/* Halcyon House — shared frontend helpers */

// Same-origin now that the frontend is served by the Spring Boot app itself.
// (If you ever split them again, set this back to an absolute URL like
// "http://localhost:8080/api".)
const API_BASE = "/api";

/* ---------------- Session (JWT stored in localStorage) ---------------- */
function getSession() {
  try { return JSON.parse(localStorage.getItem("halcyon_session")); } catch (e) { return null; }
}
function setSession(session) { localStorage.setItem("halcyon_session", JSON.stringify(session)); }
function clearSession() { localStorage.removeItem("halcyon_session"); }
/** Keep the locally-cached session in sync after a profile edit (name/email can change). */
function updateSessionProfile(account) {
  const session = getSession();
  if (!session) return;
  session.name = account.name;
  session.email = account.email;
  setSession(session);
}
function logout() { clearSession(); window.location.href = "index.html"; }

/** Redirect to login if not signed in, or if role isn't allowed on this page. */
function requireAuth(allowedRoles) {
  const session = getSession();
  if (!session || !session.token) { window.location.href = "index.html"; return null; }
  if (allowedRoles && !allowedRoles.includes(session.role)) {
    window.location.href = "index.html"; return null;
  }
  return session;
}

/* ---------------- API client ---------------- */
async function api(path, { method = "GET", body } = {}) {
  const session = getSession();
  const headers = { "Content-Type": "application/json" };
  if (session && session.token) headers["Authorization"] = "Bearer " + session.token;

  const res = await fetch(API_BASE + path, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;
  const isJson = (res.headers.get("content-type") || "").includes("application/json");
  const data = isJson ? await res.json().catch(() => null) : null;

  if (!res.ok) {
    const message = (data && data.message) || `Request failed (${res.status})`;
    throw new Error(message);
  }
  return data;
}

/* ---------------- Formatting ---------------- */
function fmtMoney(n) {
  const num = Number(n || 0);
  return "LKR " + num.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function fmtDate(d) {
  if (!d) return "—";
  return new Date(d).toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" });
}
function todayISO() { return new Date().toISOString().slice(0, 10); }
function escapeHtml(s) {
  return String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}
function toneFor(status) {
  const map = {
    ACTIVE: "green", AVAILABLE: "green", PAID: "green", CHECKED_IN: "green",
    CONFIRMED: "amber", UNPAID: "amber", OCCUPIED: "amber", ON_LEAVE: "amber",
    MAINTENANCE: "rust", CANCELLED: "rust", INACTIVE: "rust",
    REFUNDED: "grey", CHECKED_OUT: "grey", VOID: "grey",
  };
  return map[status] || "grey";
}
function pill(status) {
  return `<span class="hh-pill pill-${toneFor(status)}">${escapeHtml((status || "").toLowerCase().replace("_", " "))}</span>`;
}

/* ---------------- Sidebar / shell ---------------- */
const NAV = {
  ADMIN: [
    { href: "dashboard.html", label: "Dashboard", key: "dashboard" },
    { href: "accounts.html", label: "Accounts", key: "accounts" },
    { href: "rooms.html", label: "Rooms", key: "rooms" },
    { href: "bookings.html", label: "Bookings", key: "bookings" },
    { href: "payments.html", label: "Payments", key: "payments" },
    { href: "staff.html", label: "Staff", key: "staff" },
    { href: "profile.html", label: "My profile", key: "profile" },
  ],
  STAFF: [
    { href: "dashboard.html", label: "Dashboard", key: "dashboard" },
    { href: "rooms.html", label: "Rooms", key: "rooms" },
    { href: "bookings.html", label: "Bookings", key: "bookings" },
    { href: "profile.html", label: "My profile", key: "profile" },
  ],
  CUSTOMER: [
    { href: "browse.html", label: "Browse rooms", key: "browse" },
    { href: "my-bookings.html", label: "My bookings", key: "mybookings" },
    { href: "invoices.html", label: "My invoices", key: "invoices" },
    { href: "profile.html", label: "My profile", key: "profile" },
  ],
};

/* ---------------- Demo cards (for the Card payment method) ----------------
   These are real Stripe *test* card numbers (never live cards) — handy to
   paste into the "Add card" form so the saved-cards flow has something
   realistic to work with straight away, without inventing your own numbers.
   Visa: 4242 4242 4242 4242   Mastercard: 5555 5555 5555 4444   Amex: 3782 822463 10005 */

function renderShell(activeKey, session) {
  const items = NAV[session.role] || [];
  const initials = session.name.split(" ").map((s) => s[0]).slice(0, 2).join("").toUpperCase();
  const nav = items.map((it) =>
    `<a class="hh-navitem ${it.key === activeKey ? "active" : ""}" href="${it.href}">${escapeHtml(it.label)}</a>`
  ).join("");

  document.getElementById("sidebar").innerHTML = `
    <div class="hh-brand">
      <div class="hh-brand-mark"></div>
      <div><div class="hh-brand-text">Halcyon House</div><div class="hh-brand-sub">HOTEL OPERATIONS</div></div>
    </div>
    <div class="hh-navgroup">MENU</div>
    ${nav}
    <div class="hh-side-foot">
      <div class="hh-user">
        <div class="hh-avatar">${initials}</div>
        <div><div style="font-size:13px;font-weight:600;">${escapeHtml(session.name)}</div>
        <div style="font-size:11px;color:#A6997E;text-transform:capitalize;">${session.role.toLowerCase()}</div></div>
      </div>
      <button class="hh-navitem" onclick="logout()">Sign out</button>
    </div>`;
}

function toast(msg, type = "error") {
  const el = document.getElementById("hh-toast");
  if (!el) { alert(msg); return; }
  el.textContent = msg;
  el.className = type === "error" ? "hh-error" : "hh-success";
  el.style.display = "block";
  clearTimeout(el._t);
  el._t = setTimeout(() => { el.style.display = "none"; }, 4000);
}


/* =====================================================================
   Card UI helpers (live card preview, formatting, validation)
   ===================================================================== */
function detectCardBrand(d) {
  if (/^4/.test(d)) return "Visa";
  if (/^5[1-5]/.test(d)) return "Mastercard";
  if (/^3[47]/.test(d)) return "American Express";
  if (/^6/.test(d)) return "Discover";
  return "";
}
function formatCardNumber(digits, brand) {
  if (brand === "American Express") {
    return [digits.slice(0, 4), digits.slice(4, 10), digits.slice(10, 15)].filter(Boolean).join(" ");
  }
  return digits.replace(/(.{4})/g, "$1 ").trim();
}
/** "MM/YY" -> {month, year} or null when it isn't a real, unexpired date. */
function parseExpiry(value) {
  const m = /^(\d{2})\/(\d{2})$/.exec((value || "").trim());
  if (!m) return null;
  const month = Number(m[1]), year = 2000 + Number(m[2]);
  if (month < 1 || month > 12) return null;
  const now = new Date();
  if (year < now.getFullYear() || (year === now.getFullYear() && month < now.getMonth() + 1)) return null;
  return { month, year };
}
function luhnOk(d) {
  let sum = 0, alt = false;
  for (let i = d.length - 1; i >= 0; i--) {
    let n = Number(d[i]);
    if (alt) { n *= 2; if (n > 9) n -= 9; }
    sum += n; alt = !alt;
  }
  return d.length >= 12 && sum % 10 === 0;
}
function cardPreviewHTML(p) {
  return `
  <div class="hh-cc-scene">
    <div class="hh-cc" id="${p}Cc" data-brand="">
      <div class="hh-cc-face">
        <div class="hh-cc-top"><div class="hh-cc-chip"></div><div class="hh-cc-brand" id="${p}PrevBrand">Halcyon Pay</div></div>
        <div class="hh-cc-num" id="${p}PrevNum"></div>
        <div class="hh-cc-bottom">
          <div><div class="hh-cc-label">Card holder</div><div class="hh-cc-val" id="${p}PrevName">YOUR NAME</div></div>
          <div style="text-align:right"><div class="hh-cc-label">Expires</div><div class="hh-cc-val" id="${p}PrevExp">MM/YY</div></div>
        </div>
      </div>
      <div class="hh-cc-face hh-cc-back">
        <div class="hh-cc-strip"></div>
        <div class="hh-cc-sig"><div class="bar"></div><div class="cvv" id="${p}PrevCvv">•••</div></div>
        <div class="hh-cc-note">Demo only — your CVV is never stored.</div>
      </div>
    </div>
  </div>`;
}
function renderCardNumberPreview(p, digits, brand) {
  const el = document.getElementById(p + "PrevNum");
  const padded = (brand === "American Express" ? digits.padEnd(15, "•") : digits.padEnd(16, "•"));
  const text = formatCardNumber(padded, brand);
  const prev = el.dataset.v || "";
  el.innerHTML = [...text].map((ch, i) => `<span class="${ch !== prev[i] && ch !== "•" && ch !== " " ? "pop" : ""}">${ch === " " ? "&nbsp;" : ch}</span>`).join("");
  el.dataset.v = text;
}
/** Wire up inputs named {p}Name, {p}Number, {p}Expiry, {p}Cvv to the preview {p}Cc. */
function initCardForm(p) {
  const $ = (s) => document.getElementById(p + s);
  const cc = $("Cc");
  $("Number").addEventListener("input", function () {
    const digits = this.value.replace(/\D/g, "").slice(0, 16);
    const brand = detectCardBrand(digits);
    this.value = formatCardNumber(digits, brand);
    cc.dataset.brand = brand;
    $("PrevBrand").textContent = brand || "Halcyon Pay";
    $("Cvv").maxLength = brand === "American Express" ? 4 : 3;
    renderCardNumberPreview(p, digits, brand);
    this.classList.remove("invalid");
  });
  $("Name").addEventListener("input", function () { $("PrevName").textContent = this.value.toUpperCase() || "YOUR NAME"; this.classList.remove("invalid"); });
  $("Expiry").addEventListener("input", function () {
    let v = this.value.replace(/\D/g, "").slice(0, 4);
    if (v.length >= 2) v = v.slice(0, 2) + "/" + v.slice(2);
    this.value = v;
    $("PrevExp").textContent = v || "MM/YY";
    this.classList.remove("invalid");
  });
  $("Cvv").addEventListener("input", function () { this.value = this.value.replace(/\D/g, ""); $("PrevCvv").textContent = this.value.padEnd(3, "•"); this.classList.remove("invalid"); });
  $("Cvv").addEventListener("focus", () => cc.classList.add("flipped"));
  $("Cvv").addEventListener("blur", () => cc.classList.remove("flipped"));
  resetCardForm(p);
}
function resetCardForm(p) {
  const $ = (s) => document.getElementById(p + s);
  ["Name", "Number", "Expiry", "Cvv"].forEach((s) => { $(s).value = ""; $(s).classList.remove("invalid"); });
  $("Cc").dataset.brand = ""; $("Cc").classList.remove("flipped");
  $("PrevBrand").textContent = "Halcyon Pay"; $("PrevName").textContent = "YOUR NAME";
  $("PrevExp").textContent = "MM/YY"; $("PrevCvv").textContent = "•••";
  $("PrevNum").dataset.v = ""; renderCardNumberPreview(p, "", "");
}
/** Validates the form, marking bad fields. Returns the API payload or null. */
function readCardForm(p, { needCvv }) {
  const $ = (s) => document.getElementById(p + s);
  const bad = (s) => { $(s).classList.remove("invalid"); void $(s).offsetWidth; $(s).classList.add("invalid"); };
  const name = $("Name").value.trim();
  const digits = $("Number").value.replace(/\D/g, "");
  const exp = parseExpiry($("Expiry").value);
  const cvv = $("Cvv").value;
  let ok = true;
  if (!name) { bad("Name"); ok = false; }
  if (!luhnOk(digits)) { bad("Number"); ok = false; }
  if (!exp) { bad("Expiry"); ok = false; }
  if (needCvv && !/^\d{3,4}$/.test(cvv)) { bad("Cvv"); ok = false; }
  if (!ok) return null;
  return { cardholderName: name, cardNumber: digits, expiryMonth: exp.month, expiryYear: exp.year, cvv };
}
function miniCardHTML(c, { selectable = false, selected = false, onclick = "", onDelete = "" } = {}) {
  return `
  <div class="hh-mini ${selectable ? "selectable" : ""} ${selected ? "selected" : ""}" data-brand="${escapeHtml(c.brand)}" ${onclick ? `onclick="${onclick}"` : ""}>
    <div class="m-brand">${escapeHtml(c.brand)}</div>
    <div>
      <div class="m-num">•••• •••• •••• ${escapeHtml(c.last4)}</div>
      <div class="m-foot"><span>${escapeHtml(c.cardholderName || "")}</span><span>${String(c.expiryMonth).padStart(2, "0")}/${String(c.expiryYear).slice(-2)}</span></div>
    </div>
    ${onDelete ? `<button class="m-del" title="Remove card" onclick="event.stopPropagation();${onDelete}">🗑</button>` : ""}
  </div>`;
}
function cardChipHTML(brand, last4) {
  return `<span class="hh-card-chip" data-brand="${escapeHtml(brand || "Card")}">💳 ${escapeHtml(brand || "Card")} •••• ${escapeHtml(last4 || "")}</span>`;
}

/* click ripple on every .hh-btn */
document.addEventListener("click", (e) => {
  const btn = e.target.closest && e.target.closest(".hh-btn");
  if (!btn || btn.disabled) return;
  const r = btn.getBoundingClientRect(), size = Math.max(r.width, r.height);
  const dot = document.createElement("span");
  dot.className = "ripple";
  dot.style.cssText = `width:${size}px;height:${size}px;left:${e.clientX - r.left - size / 2}px;top:${e.clientY - r.top - size / 2}px`;
  btn.appendChild(dot);
  setTimeout(() => dot.remove(), 650);
});
