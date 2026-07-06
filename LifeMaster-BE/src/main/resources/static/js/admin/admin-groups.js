let allGroups = [];
let filteredGroups = [];

let groupCurrentPage = 1;
let selectedGroupId = null;

let groupProgressCurrentPage = 0;
let groupProgressTotalPages = 1;

const groupPageSize = 10;
const groupProgressPageSize = 10;

/* =========================
   전체 그룹 조회
========================= */

async function loadGroups() {
    const response = await fetch("/admin/groups/dashboard", {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("그룹 조회 실패");
        return;
    }

    const data = await response.json();

    allGroups = data.groups || [];

    updateGroupSummary(data.summary);
    applyGroupSearch();
}

function updateGroupSummary(summary) {
    const total = summary?.totalGroups || 0;
    const abnormal = summary?.abnormalGroups || 0;
    const normal = total - abnormal;

    document.getElementById("groupTotalCount").innerText = total;
    document.getElementById("groupNormalCount").innerText = normal;
    document.getElementById("groupPrivateCount").innerText = summary?.privateGroups || 0;
    document.getElementById("groupAbnormalCount").innerText = abnormal;
}

function renderGroups() {
    const tbody = document.getElementById("groupTableBody");
    tbody.innerHTML = "";

    const start = (groupCurrentPage - 1) * groupPageSize;
    const end = start + groupPageSize;
    const pageItems = filteredGroups.slice(start, end);

    if (pageItems.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9">조회된 그룹이 없습니다.</td>
            </tr>
        `;
        updateGroupPagination();
        return;
    }

    pageItems.forEach(group => {
        const statusText = group.abnormal
            ? `<span class="status-badge red">비정상</span>`
            : `<span class="status-badge green">정상</span>`;

        const deleteButton = group.abnormal
            ? `<button class="danger-btn" onclick="event.stopPropagation(); deleteAbnormalGroup(${group.groupId})">비정상 삭제</button>`
            : `<button class="danger-btn" onclick="event.stopPropagation(); forceDeleteGroup(${group.groupId})">강제 삭제</button>`;

        const selectedClass = selectedGroupId === group.groupId
            ? "selected-group-row"
            : "";

        const row = `
            <tr class="group-row ${selectedClass}" onclick="selectGroup(${group.groupId})">
                <td>${group.groupId}</td>
                <td>${escapeHtml(group.name || "-")}</td>
                <td>${group.accessType || "-"}</td>
                <td>${escapeHtml(group.ownerEmail || "-")}</td>
                <td>${group.memberCount ?? 0}</td>
                <td>${group.goalCount ?? 0}</td>
                <td>${statusText}</td>
                <td>${escapeHtml(group.abnormalReason || "-")}</td>
                <td>${deleteButton}</td>
            </tr>
        `;

        tbody.insertAdjacentHTML("beforeend", row);
    });

    updateGroupPagination();
}

/* =========================
   그룹 검색
========================= */

function applyGroupSearch() {
    const input = document.getElementById("groupSearchInput");

    const keyword = input
        ? input.value.trim().toLowerCase()
        : "";

    if (!keyword) {
        filteredGroups = [...allGroups];
    } else {
        filteredGroups = allGroups.filter(group => {
            const groupId = String(group.groupId ?? "");
            const groupName = String(group.name ?? "").toLowerCase();

            return (
                groupId === keyword ||
                groupName.includes(keyword)
            );
        });
    }

    groupCurrentPage = 1;
    renderGroups();
}

function searchGroups() {
    applyGroupSearch();
}

function resetGroupSearch() {
    const input = document.getElementById("groupSearchInput");

    if (input) {
        input.value = "";
    }

    filteredGroups = [...allGroups];
    groupCurrentPage = 1;
    renderGroups();
}

function handleGroupSearchEnter(event) {
    if (event.key === "Enter") {
        searchGroups();
    }
}

/* =========================
   그룹 선택
========================= */

async function selectGroup(groupId) {
    selectedGroupId = groupId;
    groupProgressCurrentPage = 0;

    renderGroups();

    await Promise.all([
        loadGroupMembers(groupId),
        loadGroupActivity(groupId),
        loadGroupGoalProgress(groupId, 0)
    ]);
}

/* =========================
   그룹 페이징
========================= */

function updateGroupPagination() {
    const totalPages = Math.max(
        1,
        Math.ceil(filteredGroups.length / groupPageSize)
    );

    document.getElementById("groupPageInfo").innerText =
        `${groupCurrentPage} / ${totalPages}`;

    document.getElementById("groupPrevBtn").disabled =
        groupCurrentPage <= 1;

    document.getElementById("groupNextBtn").disabled =
        groupCurrentPage >= totalPages;
}

function prevGroupPage() {
    if (groupCurrentPage > 1) {
        groupCurrentPage--;
        renderGroups();
    }
}

function nextGroupPage() {
    const totalPages = Math.max(
        1,
        Math.ceil(filteredGroups.length / groupPageSize)
    );

    if (groupCurrentPage < totalPages) {
        groupCurrentPage++;
        renderGroups();
    }
}

/* =========================
   그룹 인원 조회
========================= */

async function loadGroupMembers(groupId) {
    const response = await fetch(`/admin/groups/${groupId}/members`, {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("그룹 인원 조회 실패");
        return;
    }

    const members = await response.json();
    const tbody = document.getElementById("groupMemberTableBody");

    tbody.innerHTML = "";

    if (!members || members.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3">그룹 인원이 없습니다.</td>
            </tr>
        `;
        return;
    }

    members.forEach(member => {
        tbody.insertAdjacentHTML("beforeend", `
            <tr>
                <td>${member.memberId}</td>
                <td>${escapeHtml(member.email || "-")}</td>
                <td>${escapeHtml(member.nickname || "-")}</td>
            </tr>
        `);
    });
}

/* =========================
   그룹 활동 조회
========================= */

async function loadGroupActivity(groupId) {
    const response = await fetch(`/admin/groups/${groupId}/activity`, {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("그룹 활동 조회 실패");
        return;
    }

    const activity = await response.json();
    const box = document.getElementById("groupActivityBox");

    box.innerHTML = `
        <div class="activity-row"><strong>그룹 ID:</strong> ${activity.groupId}</div>
        <div class="activity-row"><strong>그룹명:</strong> ${escapeHtml(activity.groupName || "-")}</div>
        <div class="activity-row"><strong>인원 수:</strong> ${activity.memberCount ?? 0}</div>
        <div class="activity-row"><strong>목표 수:</strong> ${activity.goalCount ?? 0}</div>
        <div class="activity-row"><strong>통계 목표 수:</strong> ${activity.statisticGoalCount ?? 0}</div>
        <div class="activity-row"><strong>비정상 여부:</strong> ${activity.abnormal ? "비정상" : "정상"}</div>
        <div class="activity-row"><strong>사유:</strong> ${escapeHtml(activity.abnormalReason || "-")}</div>
    `;
}

/* =========================
   그룹 삭제
========================= */

async function deleteAbnormalGroup(groupId) {
    if (!confirm(`비정상 그룹 ID ${groupId}를 삭제하시겠습니까?`)) {
        return;
    }

    const response = await fetch(`/admin/groups/${groupId}/abnormal`, {
        method: "DELETE",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const message = await response.text();
        alert(message || "비정상 그룹 삭제 실패");
        return;
    }

    alert("비정상 그룹이 삭제되었습니다.");

    selectedGroupId = null;
    clearGroupDetailPanels();
    await loadGroups();
}

async function forceDeleteGroup(groupId) {
    if (!confirm(`그룹 ID ${groupId}를 관리자 권한으로 강제 삭제하시겠습니까?`)) {
        return;
    }

    const response = await fetch(`/admin/groups/${groupId}/force`, {
        method: "DELETE",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const message = await response.text();
        alert(message || "그룹 강제 삭제 실패");
        return;
    }

    alert("그룹이 삭제되었습니다.");

    selectedGroupId = null;
    clearGroupDetailPanels();
    await loadGroups();
}

function clearGroupDetailPanels() {
    document.getElementById("groupMemberTableBody").innerHTML = `
        <tr>
            <td colspan="3">그룹을 선택하세요.</td>
        </tr>
    `;

    document.getElementById("groupActivityBox").innerText =
        "그룹을 선택하세요.";

    document.getElementById("groupGoalProgressTableBody").innerHTML = `
        <tr>
            <td colspan="9">그룹을 선택하세요.</td>
        </tr>
    `;

    groupProgressCurrentPage = 0;
    groupProgressTotalPages = 1;
    updateGroupProgressPagination();
}

/* =========================
   그룹 목표 수행 기록
========================= */

async function loadGroupGoalProgress(groupId, page = 0) {
    const tbody = document.getElementById("groupGoalProgressTableBody");

    const response = await fetch(
        `/admin/groups/${groupId}/goal-progress?page=${page}&size=${groupProgressPageSize}`,
        {
            method: "GET",
            headers: authHeaders()
        }
    );

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9">목표 수행 기록 조회에 실패했습니다.</td>
            </tr>
        `;
        return;
    }

    const data = await response.json();

    if (selectedGroupId !== groupId) return;

    groupProgressCurrentPage = data.number ?? 0;
    groupProgressTotalPages = Math.max(data.totalPages || 1, 1);

    tbody.innerHTML = "";

    if (!data.content || data.content.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9">목표 수행 기록이 없습니다.</td>
            </tr>
        `;
    } else {
        data.content.forEach(progress => {
            tbody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${progress.progressId}</td>
                    <td>${progress.userId}</td>
                    <td>${escapeHtml(progress.userEmail || "-")}</td>
                    <td>${escapeHtml(progress.userNickname || "-")}</td>
                    <td>${escapeHtml(progress.goalName || "-")}</td>
                    <td>${escapeHtml(progress.goalType || "-")}</td>
                    <td>${escapeHtml(progress.goalDuration || "-")}</td>
                    <td>${formatProgressValue(progress.progressValue)}</td>
                    <td>${formatSubmittedAt(progress.submittedAt)}</td>
                </tr>
            `);
        });
    }

    updateGroupProgressPagination();
}

function updateGroupProgressPagination() {
    const pageInfo = document.getElementById("groupProgressPageInfo");
    const prevBtn = document.getElementById("groupProgressPrevBtn");
    const nextBtn = document.getElementById("groupProgressNextBtn");

    if (!pageInfo || !prevBtn || !nextBtn) {
        return;
    }

    pageInfo.innerText =
        `${groupProgressCurrentPage + 1} / ${groupProgressTotalPages}`;

    prevBtn.disabled =
        groupProgressCurrentPage <= 0;

    nextBtn.disabled =
        groupProgressCurrentPage >= groupProgressTotalPages - 1;
}

function prevGroupProgressPage() {
    if (selectedGroupId !== null && groupProgressCurrentPage > 0) {
        loadGroupGoalProgress(
            selectedGroupId,
            groupProgressCurrentPage - 1
        );
    }
}

function nextGroupProgressPage() {
    if (
        selectedGroupId !== null &&
        groupProgressCurrentPage < groupProgressTotalPages - 1
    ) {
        loadGroupGoalProgress(
            selectedGroupId,
            groupProgressCurrentPage + 1
        );
    }
}

/* =========================
   Format / Util
========================= */

function formatProgressValue(value) {
    const number = Number(value);

    if (!Number.isFinite(number)) {
        return "-";
    }

    return number.toLocaleString("ko-KR", {
        maximumFractionDigits: 2
    });
}

function formatSubmittedAt(value) {
    if (!value) {
        return "-";
    }

    return escapeHtml(
        String(value)
            .replace("T", " ")
            .substring(0, 19)
    );
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}