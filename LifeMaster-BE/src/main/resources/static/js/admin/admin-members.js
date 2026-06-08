let memberCurrentPage = 0;
let memberTotalPages = 1;

const memberPageSize = 10;

/* =========================
   전체 회원 조회
========================= */

async function loadMembers() {
    const response = await fetch(
        `/admin/members?page=${memberCurrentPage}&size=${memberPageSize}`,
        {
            method: "GET",
            headers: authHeaders()
        }
    );

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("회원 조회 실패");
        return;
    }

    const result = await response.json();

    memberCurrentPage = result.number ?? 0;
    memberTotalPages = result.totalPages || 1;

    renderMembers(result.content ?? []);
    updateMemberPagination();
}

function renderMembers(members) {
    const tbody = document.getElementById("memberTableBody");
    tbody.innerHTML = "";

    if (!members || members.length === 0) {
        tbody.innerHTML =
            `<tr><td colspan="13" class="empty">조회된 회원이 없습니다.</td></tr>`;
        return;
    }

    members.forEach(member => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${member.memberId ?? "-"}</td>
            <td>${member.email ?? "-"}</td>
            <td>${member.nickname ?? "-"}</td>
            <td>${member.loginType ?? "-"}</td>
            <td>${loginStatusBadge(member.loginStatus)}</td>
            <td>${roleBadge(member.loginRole)}</td>
            <td>${memberStatusBadge(member.memberStatus)}</td>
            <td>${member.warningCount ?? 0}</td>
            <td>${subscriptionBadge(member.subscriptionPlan)}</td>
            <td>${paymentStatusBadge(member.paymentStatus)}</td>
            <td>${formatDate(member.expirationDate)}</td>
            <td>${formatDateTime(member.createdAt)}</td>
            <td>${memberActionButtons(member)}</td>
        `;

        tbody.appendChild(tr);
    });
}

function memberActionButtons(member) {
    return `
        <div class="action-buttons">
            ${adminRoleActionButton(member)}
            ${extendSubscriptionButton(member)}
            ${revokeSubscriptionButton(member)}
        </div>
    `;
}

function revokeSubscriptionButton(member) {
    if (member.subscriptionPlan !== "PREMIUM") {
        return "";
    }

    return `
        <button 
            class="danger-btn"
            onclick="revokeMemberSubscription(${member.memberId})"
        >
            구독 회수
        </button>
    `;
}

function extendSubscriptionButton(member) {
    return `
        <button 
            class="primary-btn"
            onclick="extendMemberSubscriptionOneMonth(${member.memberId})"
        >
            1개월 연장
        </button>
    `;
}

function adminRoleActionButton(member) {
    if (member.loginRole === "ADMIN") {
        return `
            <button 
                class="danger-btn"
                onclick="revokeAdminRole(${member.memberId})"
            >
                권한 회수
            </button>
        `;
    }

    return `
        <button 
            class="primary-btn"
            onclick="grantAdminRole(${member.memberId})"
        >
            어드민 부여
        </button>
    `;
}

async function grantAdminRole(memberId) {
    const confirmed = confirm("이 회원에게 관리자 권한을 부여하시겠습니까?");
    if (!confirmed) return;

    const response = await fetch(`/admin/members/${memberId}/admin-role/grant`, {
        method: "PATCH",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const errorText = await response.text();
        alert("관리자 권한 부여 실패\n" + errorText);
        return;
    }

    alert("관리자 권한이 부여되었습니다.");

    await loadMembers();
    await loadMemberSummary();
}

async function revokeAdminRole(memberId) {
    const confirmed = confirm("이 회원의 관리자 권한을 회수하시겠습니까?");
    if (!confirmed) return;

    const response = await fetch(`/admin/members/${memberId}/admin-role/revoke`, {
        method: "PATCH",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const errorText = await response.text();
        alert("관리자 권한 회수 실패\n" + errorText);
        return;
    }

    alert("관리자 권한이 회수되었습니다.");

    await loadMembers();
    await loadMemberSummary();
}

/* =========================
   페이징
========================= */

function updateMemberPagination() {
    document.getElementById("memberPageInfo").textContent =
        `${memberCurrentPage + 1} / ${memberTotalPages}`;

    document.getElementById("memberPrevBtn").disabled =
        memberCurrentPage <= 0;

    document.getElementById("memberNextBtn").disabled =
        memberCurrentPage >= memberTotalPages - 1;
}

function prevMemberPage() {
    if (memberCurrentPage > 0) {
        memberCurrentPage--;
        loadMembers();
    }
}

function nextMemberPage() {
    if (memberCurrentPage < memberTotalPages - 1) {
        memberCurrentPage++;
        loadMembers();
    }
}

function reloadMembers() {
    memberCurrentPage = 0;
    loadMembers();
    loadMemberSummary();
}

/* =========================
   상단 요약 카드
========================= */

async function loadMemberSummary() {
    const response = await fetch("/admin/members/summary", {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("회원 요약 정보 조회 실패");
        return;
    }

    const summary = await response.json();

    document.getElementById("memberTotalCount").textContent =
        summary.totalMemberCount ?? 0;

    document.getElementById("memberActiveCount").textContent =
        summary.activeMemberCount ?? 0;

    document.getElementById("memberPremiumCount").textContent =
        summary.premiumMemberCount ?? 0;

    document.getElementById("memberAdminCount").textContent =
        summary.adminMemberCount ?? 0;
}

/* =========================
   Badge
========================= */

function loginStatusBadge(status) {
    if (status === true) {
        return `<span class="badge green">LOGIN</span>`;
    }

    return `<span class="badge gray">LOGOUT</span>`;
}

function roleBadge(role) {
    if (role === "ADMIN") {
        return `<span class="badge red">ADMIN</span>`;
    }

    return `<span class="badge blue-badge">${role ?? "USER"}</span>`;
}

function memberStatusBadge(status) {
    if (status === "ACTIVE") {
        return `<span class="badge green">ACTIVE</span>`;
    }

    return `<span class="badge gray">${status ?? "-"}</span>`;
}

function subscriptionBadge(plan) {
    if (plan === "PREMIUM") {
        return `<span class="badge yellow">PREMIUM</span>`;
    }

    return `<span class="badge gray">${plan ?? "FREE"}</span>`;
}

function paymentStatusBadge(status) {
    if (status === "PAID") {
        return `<span class="badge green">PAID</span>`;
    }

    return `<span class="badge gray">${status ?? "-"}</span>`;
}

async function extendMemberSubscriptionOneMonth(memberId) {
    const confirmed = confirm("이 회원의 구독 기한을 1개월 연장하시겠습니까?");
    if (!confirmed) return;

    const response = await fetch(
        `/admin/members/${memberId}/subscription/extend-one-month`,
        {
            method: "PATCH",
            headers: authHeaders()
        }
    );

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const errorText = await response.text();
        alert("구독 기한 1개월 연장 실패\n" + errorText);
        return;
    }

    alert("구독 기한이 1개월 연장되었습니다.");

    await loadMembers();
    await loadMemberSummary();

    // 프리미엄 계정 페이지도 같이 쓰고 있다면 갱신
    if (typeof loadPremiumMembers === "function") {
        await loadPremiumMembers();
    }
}

async function revokeMemberSubscription(memberId) {
    const confirmed = confirm("이 회원의 프리미엄 구독을 회수하시겠습니까?");
    if (!confirmed) return;

    const response = await fetch(
        `/admin/members/${memberId}/subscription/revoke`,
        {
            method: "PATCH",
            headers: authHeaders()
        }
    );

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const errorText = await response.text();
        alert("구독 회수 실패\n" + errorText);
        return;
    }

    alert("구독이 회수되었습니다.");

    await loadMembers();
    await loadMemberSummary();

    if (typeof loadPremiumMembers === "function") {
        await loadPremiumMembers();
    }
}