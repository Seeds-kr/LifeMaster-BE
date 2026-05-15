let nonPremiumCurrentPage = 0;
let nonPremiumTotalPages = 1;

const nonPremiumPageSize = 10;

async function loadPremiumMembers() {
    const response = await fetch("/admin/premium/premium-members", {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("프리미엄 계정 조회 실패");
        return;
    }

    const members = await response.json();

    renderPremiumMembers(members);
    updatePremiumSummary(members);
}

function renderPremiumMembers(members) {
    const tbody = document.getElementById("premiumTableBody");
    tbody.innerHTML = "";

    if (!members || members.length === 0) {
        tbody.innerHTML =
            `<tr><td colspan="6" class="empty">조회된 프리미엄 회원이 없습니다.</td></tr>`;
        return;
    }

    members.forEach(member => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${member.memberId ?? "-"}</td>
            <td>${member.email ?? "-"}</td>
            <td>${member.name ?? "-"}</td>
            <td>${formatDate(member.subscriptionExpirationDate)}</td>
            <td>${premiumStatusBadge(member.expired)}</td>
            <td>
                <button 
                    class="danger-btn"
                    onclick="revokePremium(${member.memberId})"
                >
                    권한 회수
                </button>
            </td>
        `;

        tbody.appendChild(tr);
    });
}

function updatePremiumSummary(members) {
    const now = new Date();
    const sevenDaysLater = new Date();
    sevenDaysLater.setDate(now.getDate() + 7);

    const activeMembers = members.filter(m => m.expired !== true);
    const expiredMembers = members.filter(m => m.expired === true);

    const expireSoonMembers = members.filter(m => {
        if (m.expired === true || !m.subscriptionExpirationDate) return false;

        const expireDate = new Date(m.subscriptionExpirationDate);
        if (isNaN(expireDate.getTime())) return false;

        return expireDate >= now && expireDate <= sevenDaysLater;
    });

    document.getElementById("premiumTotalCount").textContent = members.length;
    document.getElementById("premiumActiveCount").textContent = activeMembers.length;
    document.getElementById("premiumExpireSoonCount").textContent = expireSoonMembers.length;
    document.getElementById("premiumExpiredCount").textContent = expiredMembers.length;
}

function premiumStatusBadge(expired) {
    if (expired === true) {
        return `<span class="badge red">EXPIRED</span>`;
    }

    return `<span class="badge green">ACTIVE</span>`;
}

/* =========================
   일반 회원 조회
========================= */

async function loadNonPremiumMembers(page = 0) {
    const response = await fetch(
        `/admin/premium/non-premium-members?page=${page}&size=${nonPremiumPageSize}`,
        {
            method: "GET",
            headers: authHeaders()
        }
    );

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("일반 회원 조회 실패");
        return;
    }

    const result = await response.json();

    nonPremiumCurrentPage = result.number ?? 0;
    nonPremiumTotalPages = result.totalPages || 1;

    renderNonPremiumMembers(result.content ?? []);
    updateNonPremiumPagination();
}

function renderNonPremiumMembers(members) {
    const tbody = document.getElementById("nonPremiumTableBody");
    tbody.innerHTML = "";

    if (!members || members.length === 0) {
        tbody.innerHTML =
            `<tr><td colspan="5" class="empty">조회된 일반 회원이 없습니다.</td></tr>`;
        return;
    }

    members.forEach(member => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${member.memberId ?? "-"}</td>
            <td>${member.email ?? "-"}</td>
            <td>${member.name ?? "-"}</td>
            <td>
                <select id="grantType-${member.memberId}" class="premium-grant-select">
                    <option value="ONE_MONTH">1개월</option>
                    <option value="THIRTEEN_MONTHS">13개월</option>
                    <option value="PERMANENT">영구</option>
                </select>
            </td>
            <td>
                <button 
                    class="primary-btn"
                    onclick="grantPremium(${member.memberId})"
                >
                    권한 부여
                </button>
            </td>
        `;

        tbody.appendChild(tr);
    });
}

function updateNonPremiumPagination() {
    document.getElementById("nonPremiumPageInfo").textContent =
        `${nonPremiumCurrentPage + 1} / ${nonPremiumTotalPages}`;

    document.getElementById("nonPremiumPrevBtn").disabled =
        nonPremiumCurrentPage <= 0;

    document.getElementById("nonPremiumNextBtn").disabled =
        nonPremiumCurrentPage >= nonPremiumTotalPages - 1;
}

function prevNonPremiumPage() {
    if (nonPremiumCurrentPage > 0) {
        loadNonPremiumMembers(nonPremiumCurrentPage - 1);
    }
}

function nextNonPremiumPage() {
    if (nonPremiumCurrentPage < nonPremiumTotalPages - 1) {
        loadNonPremiumMembers(nonPremiumCurrentPage + 1);
    }
}

/* =========================
   프리미엄 권한 부여
========================= */

async function grantPremium(memberId) {
    const select = document.getElementById(`grantType-${memberId}`);
    const grantType = select.value;

    const confirmed = confirm("이 회원에게 프리미엄 권한을 부여하시겠습니까?");
    if (!confirmed) return;

    const response = await fetch(`/admin/premium/${memberId}/grant`, {
        method: "POST",
        headers: {
            ...authHeaders(),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            grantType: grantType
        })
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const errorText = await response.text();
        alert("프리미엄 권한 부여 실패\n" + errorText);
        return;
    }

    alert("프리미엄 권한이 부여되었습니다.");

    await loadPremiumMembers();
    await loadNonPremiumMembers(nonPremiumCurrentPage);
}

/* =========================
   프리미엄 권한 회수
========================= */

async function revokePremium(memberId) {
    const confirmed = confirm("이 회원의 프리미엄 권한을 회수하시겠습니까?");
    if (!confirmed) return;

    const response = await fetch(`/admin/premium/${memberId}/revoke`, {
        method: "DELETE",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const errorText = await response.text();
        alert("프리미엄 권한 회수 실패\n" + errorText);
        return;
    }

    alert("프리미엄 권한이 회수되었습니다.");

    await loadPremiumMembers();
    await loadNonPremiumMembers(nonPremiumCurrentPage);
}