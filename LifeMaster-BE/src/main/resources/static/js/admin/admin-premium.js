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
        tbody.innerHTML = `<tr><td colspan="5" class="empty">조회된 프리미엄 회원이 없습니다.</td></tr>`;
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