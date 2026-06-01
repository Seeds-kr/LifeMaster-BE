let allGroups = [];
let groupCurrentPage = 1;
const groupPageSize = 10;

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
    groupCurrentPage = 1;

    updateGroupSummary(data.summary);
    renderGroups();
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
    const pageItems = allGroups.slice(start, end);

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
            ? `<button class="danger-btn" onclick="deleteAbnormalGroup(${group.groupId})">비정상 삭제</button>`
            : `<button class="danger-btn" onclick="forceDeleteGroup(${group.groupId})">강제 삭제</button>`;

        const row = `
            <tr>
                <td>${group.groupId}</td>
                <td>${escapeHtml(group.name || "-")}</td>
                <td>${group.accessType || "-"}</td>
                <td>${group.ownerEmail || "-"}</td>
                <td>${group.memberCount}</td>
                <td>${group.goalCount}</td>
                <td>${statusText}</td>
                <td>${group.abnormalReason || "-"}</td>
                <td>
                    <button onclick="loadGroupMembers(${group.groupId})">인원</button>
                    <button onclick="loadGroupActivity(${group.groupId})">활동</button>
                    ${deleteButton}
                </td>
            </tr>
        `;

        tbody.insertAdjacentHTML("beforeend", row);
    });

    updateGroupPagination();
}

function updateGroupPagination() {
    const totalPages = Math.max(1, Math.ceil(allGroups.length / groupPageSize));

    document.getElementById("groupPageInfo").innerText = `${groupCurrentPage} / ${totalPages}`;
    document.getElementById("groupPrevBtn").disabled = groupCurrentPage <= 1;
    document.getElementById("groupNextBtn").disabled = groupCurrentPage >= totalPages;
}

function prevGroupPage() {
    if (groupCurrentPage > 1) {
        groupCurrentPage--;
        renderGroups();
    }
}

function nextGroupPage() {
    const totalPages = Math.max(1, Math.ceil(allGroups.length / groupPageSize));

    if (groupCurrentPage < totalPages) {
        groupCurrentPage++;
        renderGroups();
    }
}

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
        <div class="activity-row"><strong>인원 수:</strong> ${activity.memberCount}</div>
        <div class="activity-row"><strong>목표 수:</strong> ${activity.goalCount}</div>
        <div class="activity-row"><strong>통계 목표 수:</strong> ${activity.statisticGoalCount}</div>
        <div class="activity-row"><strong>비정상 여부:</strong> ${activity.abnormal ? "비정상" : "정상"}</div>
        <div class="activity-row"><strong>사유:</strong> ${activity.abnormalReason || "-"}</div>
    `;
}

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
    await loadGroups();
    clearGroupDetailPanels();
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
    await loadGroups();
    clearGroupDetailPanels();
}

function clearGroupDetailPanels() {
    document.getElementById("groupMemberTableBody").innerHTML = `
        <tr>
            <td colspan="3">그룹을 선택하세요.</td>
        </tr>
    `;

    document.getElementById("groupActivityBox").innerText = "그룹을 선택하세요.";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}