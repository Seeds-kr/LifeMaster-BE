let reportCurrentPage = 0;
let reportTotalPages = 1;
const reportPageSize = 20;

// 현재 선택된 필터
let currentStatusFilter = null;

// 페이지 로드 시 신고 목록 조회
async function loadReports() {
    let url = `/admin/reports?page=${reportCurrentPage}&size=${reportPageSize}`;
    if (currentStatusFilter) {
        url += `&status=${currentStatusFilter}`;
    }

    const response = await fetch(url, {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("신고 목록 조회 실패");
        return;
    }

    const data = await response.json();
    const reports = data.content ?? [];
    reportCurrentPage = data.number ?? reportCurrentPage;
    reportTotalPages = data.totalPages ?? 1;

    renderReports(reports);
}

// 요약 통계 로드
async function loadReportSummary() {
    const statuses = ["PENDING", "REVIEWING", "RESOLVED", "REJECTED"];
    let totalCount = 0;
    let counts = {};

    for (const status of statuses) {
        const response = await fetch(`/admin/reports?page=0&size=1&status=${status}`, {
            method: "GET",
            headers: authHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            counts[status] = data.totalElements ?? 0;
            totalCount += counts[status];
        } else {
            counts[status] = 0;
        }
    }

    document.getElementById("reportTotalCount").textContent = totalCount;
    document.getElementById("reportPendingCount").textContent = counts["PENDING"];
    document.getElementById("reportReviewingCount").textContent = counts["REVIEWING"];
    document.getElementById("reportResolvedCount").textContent = counts["RESOLVED"] + counts["REJECTED"];
}

// 신고 목록 렌더링
function renderReports(reports) {
    const tbody = document.getElementById("reportTableBody");
    tbody.innerHTML = "";

    if (!reports || reports.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty">조회된 신고가 없습니다.</td></tr>`;
    } else {
        reports.forEach(report => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${report.id}</td>
                <td>${shortText(report.reason)}</td>
                <td>${reportStatusBadge(report.status)}</td>
                <td>${report.reporterNickname ?? "-"}</td>
                <td>${report.postId ?? "-"}</td>
                <td>${shortText(report.postTitle ?? "-")}</td>
                <td>${formatDateTime(report.createdAt)}</td>
                <td>
                    <button class="btn-detail" onclick="toggleDetail(${report.id})">상세</button>
                </td>
            `;
            tbody.appendChild(tr);

            // 상세 정보 행 (숨김 상태로 추가)
            const detailTr = document.createElement("tr");
            detailTr.id = `detail-${report.id}`;
            detailTr.className = "detail-row";
            detailTr.style.display = "none";
            detailTr.innerHTML = `
                <td colspan="8">
                    <div class="detail-container" id="detail-content-${report.id}">
                        <div class="loading">로딩 중...</div>
                    </div>
                </td>
            `;
            tbody.appendChild(detailTr);
        });
    }

    document.getElementById("reportPageInfo").textContent =
        `${reportCurrentPage + 1} / ${reportTotalPages || 1}`;

    document.getElementById("reportPrevBtn").disabled = reportCurrentPage <= 0;
    document.getElementById("reportNextBtn").disabled =
        reportCurrentPage >= (reportTotalPages || 1) - 1;
}

// 상세 정보 토글
async function toggleDetail(reportId) {
    const detailRow = document.getElementById(`detail-${reportId}`);

    if (detailRow.style.display === "none") {
        // 상세 정보 표시
        detailRow.style.display = "table-row";
        await loadReportDetail(reportId);
    } else {
        // 숨기기
        detailRow.style.display = "none";
    }
}

// 신고 상세 정보 로드
async function loadReportDetail(reportId) {
    const response = await fetch(`/admin/reports/${reportId}`, {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("신고 상세 조회 실패");
        return;
    }

    const report = await response.json();
    renderReportDetail(report);
}

// 신고 상세 정보 렌더링
function renderReportDetail(report) {
    const container = document.getElementById(`detail-content-${report.id}`);

    container.innerHTML = `
        <div class="detail-grid">
            <div class="detail-section">
                <h4>신고 정보</h4>
                <p><strong>신고 ID:</strong> ${report.id}</p>
                <p><strong>신고 사유:</strong> ${report.reason}</p>
                <p><strong>신고 일시:</strong> ${formatDateTime(report.createdAt)}</p>
                <p><strong>현재 상태:</strong> ${reportStatusBadge(report.status)}</p>
            </div>

            <div class="detail-section">
                <h4>신고자 정보</h4>
                <p><strong>ID:</strong> ${report.reporterId ?? "-"}</p>
                <p><strong>닉네임:</strong> ${report.reporterNickname ?? "-"}</p>
                <p><strong>이메일:</strong> ${report.reporterEmail ?? "-"}</p>
            </div>

            <div class="detail-section">
                <h4>게시글 정보</h4>
                <p><strong>게시글 ID:</strong> ${report.postId ?? "-"}</p>
                <p><strong>제목:</strong> ${report.postTitle ?? "-"}</p>
                <p><strong>작성자:</strong> ${report.postAuthorNickname ?? "-"} (ID: ${report.postAuthorId ?? "-"})</p>
                <p><strong>내용:</strong></p>
                <div class="post-content">${report.postContent ?? "-"}</div>
            </div>

            <div class="detail-section">
                <h4>처리 정보</h4>
                ${report.resolvedByNickname ? `
                    <p><strong>처리자:</strong> ${report.resolvedByNickname} (ID: ${report.resolvedById})</p>
                    <p><strong>조치:</strong> ${reportActionBadge(report.actionType)}</p>
                    <p><strong>관리자 메모:</strong> ${report.adminNote ?? "-"}</p>
                    <p><strong>처리 일시:</strong> ${formatDateTime(report.resolvedAt)}</p>
                ` : `
                    <p style="color: #999;">아직 처리되지 않았습니다.</p>
                `}
            </div>
        </div>

        <div class="action-section">
            ${report.status !== "RESOLVED" && report.status !== "REJECTED" ? `
                <div class="action-group">
                    <h4>상태 변경</h4>
                    <select id="status-select-${report.id}" class="status-select">
                        <option value="PENDING" ${report.status === "PENDING" ? "selected" : ""}>대기 중</option>
                        <option value="REVIEWING" ${report.status === "REVIEWING" ? "selected" : ""}>검토 중</option>
                        <option value="REJECTED" ${report.status === "REJECTED" ? "selected" : ""}>반려됨</option>
                    </select>
                    <button class="btn-action" onclick="updateReportStatus(${report.id})">상태 변경</button>
                </div>

                <div class="action-group">
                    <h4>조치 실행</h4>
                    <select id="action-select-${report.id}" class="action-select">
                        <option value="WARNING">경고</option>
                        <option value="POST_DELETE">게시글 삭제</option>
                        <option value="SUSPEND_1D">1일 정지</option>
                        <option value="SUSPEND_7D">7일 정지</option>
                        <option value="SUSPEND_30D">30일 정지</option>
                    </select>
                    <input type="text" id="admin-note-${report.id}" placeholder="관리자 메모 (선택)" class="admin-note-input">
                    <button class="btn-action btn-danger" onclick="processReportAction(${report.id})">조치 실행</button>
                </div>
            ` : `
                <p style="color: #28a745; font-weight: bold;">이미 처리가 완료된 신고입니다.</p>
            `}
        </div>
    `;
}

// 신고 상태 변경
async function updateReportStatus(reportId) {
    const statusSelect = document.getElementById(`status-select-${reportId}`);
    const newStatus = statusSelect.value;

    if (!confirm(`상태를 "${getStatusText(newStatus)}"로 변경하시겠습니까?`)) {
        return;
    }

    const response = await fetch(`/admin/reports/${reportId}/status`, {
        method: "PATCH",
        headers: {
            ...authHeaders(),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ status: newStatus })
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("상태 변경 실패");
        return;
    }

    alert("상태가 변경되었습니다.");
    await loadReportDetail(reportId);
    await loadReports();
    await loadReportSummary();
}

// 조치 실행
async function processReportAction(reportId) {
    const actionSelect = document.getElementById(`action-select-${reportId}`);
    const adminNoteInput = document.getElementById(`admin-note-${reportId}`);

    const actionType = actionSelect.value;
    const adminNote = adminNoteInput.value.trim();

    const actionText = getActionText(actionType);

    if (!confirm(`"${actionText}" 조치를 실행하시겠습니까?\n\n이 작업은 취소할 수 없습니다.`)) {
        return;
    }

    const response = await fetch(`/admin/reports/${reportId}/action`, {
        method: "POST",
        headers: {
            ...authHeaders(),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            actionType: actionType,
            adminNote: adminNote || null
        })
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("조치 실행 실패");
        return;
    }

    alert(`조치가 실행되었습니다: ${actionText}`);
    await loadReportDetail(reportId);
    await loadReports();
    await loadReportSummary();
}

// 상태 필터 변경
function filterByStatus() {
    const filterSelect = document.getElementById("statusFilterSelect");
    currentStatusFilter = filterSelect.value || null;
    reportCurrentPage = 0;
    loadReports();
}

// 새로고침
function reloadReports() {
    reportCurrentPage = 0;
    currentStatusFilter = null;
    document.getElementById("statusFilterSelect").value = "";
    loadReports();
    loadReportSummary();
}

// 페이징
function prevReportPage() {
    if (reportCurrentPage > 0) {
        reportCurrentPage--;
        loadReports();
    }
}

function nextReportPage() {
    if (reportCurrentPage < reportTotalPages - 1) {
        reportCurrentPage++;
        loadReports();
    }
}

// 상태 배지
function reportStatusBadge(status) {
    if (!status) return `<span class="badge gray">-</span>`;

    switch (status) {
        case "PENDING":
            return `<span class="badge yellow">대기 중</span>`;
        case "REVIEWING":
            return `<span class="badge blue-badge">검토 중</span>`;
        case "RESOLVED":
            return `<span class="badge green">처리 완료</span>`;
        case "REJECTED":
            return `<span class="badge red">반려됨</span>`;
        default:
            return `<span class="badge gray">${status}</span>`;
    }
}

// 조치 배지
function reportActionBadge(actionType) {
    if (!actionType) return "-";

    switch (actionType) {
        case "WARNING":
            return `<span class="badge yellow">경고</span>`;
        case "POST_DELETE":
            return `<span class="badge red">게시글 삭제</span>`;
        case "SUSPEND_1D":
            return `<span class="badge red">1일 정지</span>`;
        case "SUSPEND_7D":
            return `<span class="badge red">7일 정지</span>`;
        case "SUSPEND_30D":
            return `<span class="badge red">30일 정지</span>`;
        default:
            return actionType;
    }
}

// 상태 텍스트
function getStatusText(status) {
    switch (status) {
        case "PENDING": return "대기 중";
        case "REVIEWING": return "검토 중";
        case "RESOLVED": return "처리 완료";
        case "REJECTED": return "반려됨";
        default: return status;
    }
}

// 조치 텍스트
function getActionText(actionType) {
    switch (actionType) {
        case "WARNING": return "경고";
        case "POST_DELETE": return "게시글 삭제";
        case "SUSPEND_1D": return "1일 정지";
        case "SUSPEND_7D": return "7일 정지";
        case "SUSPEND_30D": return "30일 정지";
        default: return actionType;
    }
}
