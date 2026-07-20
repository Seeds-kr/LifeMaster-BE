let adminPostCurrentPage = 0;
let adminPostTotalPages = 0;
let adminPostPageSize = 10;

let adminPostKeyword = "";
let adminPostType = "";

function getAdminToken() {
    return localStorage.getItem("accessToken");
}

function getAdminPostHeaders() {
    return {
        "Authorization": "Bearer " + getAdminToken(),
        "Content-Type": "application/json"
    };
}

/**
 * 게시글 목록 조회.
 */
async function loadAdminPosts(page = adminPostCurrentPage) {
    adminPostCurrentPage = Math.max(page, 0);

    const params = new URLSearchParams();

    params.append("page", adminPostCurrentPage);
    params.append("size", adminPostPageSize);

    if (adminPostKeyword) {
        params.append("keyword", adminPostKeyword);
    }

    if (adminPostType) {
        params.append("type", adminPostType);
    }

    setAdminPostLoading();

    try {
        const response = await fetch(
            `/admin/posts?${params.toString()}`,
            {
                method: "GET",
                headers: getAdminPostHeaders()
            }
        );

        if (response.status === 401 || response.status === 403) {
            alert("관리자 로그인이 필요합니다.");
            location.href = "/admin/login";
            return;
        }

        if (!response.ok) {
            throw new Error(
                `게시글 조회 실패: ${response.status}`
            );
        }

        const data = await response.json();

        adminPostCurrentPage = data.number ?? 0;
        adminPostTotalPages = data.totalPages ?? 0;

        renderAdminPostTable(data.content ?? []);
        updateAdminPostPagination(data);

    } catch (error) {
        console.error(error);

        const tableBody =
            document.getElementById("adminPostTableBody");

        if (tableBody) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="11">
                        게시글을 불러오지 못했습니다.
                    </td>
                </tr>
            `;
        }
    }
}

/**
 * 상단 요약 통계 조회.
 */
async function loadAdminPostSummary() {
    try {
        const response = await fetch(
            "/admin/posts/summary",
            {
                method: "GET",
                headers: getAdminPostHeaders()
            }
        );

        if (!response.ok) {
            throw new Error(
                `게시글 요약 조회 실패: ${response.status}`
            );
        }

        const data = await response.json();

        setText(
            "adminPostTotalCount",
            formatNumber(data.totalCount)
        );

        setText(
            "adminPostFreeCount",
            formatNumber(data.freeCount)
        );

        setText(
            "adminPostImprovementCount",
            formatNumber(data.improvementCount)
        );

        setText(
            "adminPostCalendarSharedCount",
            formatNumber(data.calendarSharedCount)
        );

    } catch (error) {
        console.error(error);
    }
}

/**
 * 게시글 테이블 출력.
 */
function renderAdminPostTable(posts) {
    const tableBody =
        document.getElementById("adminPostTableBody");

    if (!tableBody) {
        return;
    }

    if (!posts || posts.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="11">
                    검색된 게시글이 없습니다.
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = posts.map(post => {
        const postId = post.postId;
        const title = escapeAdminPostHtml(
            post.title ?? "(제목 없음)"
        );

        const contentSummary = escapeAdminPostHtml(
            post.contentSummary ?? ""
        );

        const memberEmail = escapeAdminPostHtml(
            post.memberEmail ?? "-"
        );

        const memberNickname = escapeAdminPostHtml(
            post.memberNickname ?? "-"
        );

        const typeBadge =
            getAdminPostTypeBadge(post.type);

        const calendarSharedBadge =
            post.calendarShared
                ? `<span class="status-badge success">공유</span>`
                : `<span class="status-badge normal">미공유</span>`;

        const fileCell = createAdminPostFileCell(post.file);

        return `
            <tr>
                <td>${postId}</td>

                <td>
                    ${typeBadge}
                </td>

                <td class="post-summary-cell">
                    <strong>${title}</strong>
                    <div class="post-content-summary">
                        ${contentSummary || "-"}
                    </div>
                </td>

                <td>
                    <div>
                        <strong>${memberNickname}</strong>
                    </div>

                    <div class="sub-text">
                        회원 ID: ${post.memberId ?? "-"}
                    </div>

                    <div class="sub-text">
                        ${memberEmail}
                    </div>
                </td>

                <td>
                    ${formatNumber(post.viewCount)}
                </td>

                <td>
                    ${formatNumber(post.commentCount)}
                </td>

                <td>
                    ${formatNumber(post.likeCount)}
                </td>

                <td>
                    ${calendarSharedBadge}
                </td>

                <td>
                    ${formatAdminPostDate(post.createdAt)}
                </td>

                <td>
                    ${fileCell}
                </td>

                <td>
                    <button
                        type="button"
                        class="danger-btn"
                        onclick="deleteAdminPost(
                            ${postId},
                            '${escapeAdminPostJsString(post.title ?? "")}'
                        )">
                        삭제
                    </button>
                </td>
            </tr>
        `;
    }).join("");
}

/**
 * 게시글 검색.
 */
function searchAdminPosts() {
    const keywordInput =
        document.getElementById("adminPostSearchInput");

    const typeSelect =
        document.getElementById("adminPostTypeSelect");

    adminPostKeyword =
        keywordInput?.value?.trim() ?? "";

    adminPostType =
        typeSelect?.value ?? "";

    adminPostCurrentPage = 0;

    loadAdminPosts(0);
}

/**
 * 검색 초기화.
 */
function resetAdminPostSearch() {
    const keywordInput =
        document.getElementById("adminPostSearchInput");

    const typeSelect =
        document.getElementById("adminPostTypeSelect");

    if (keywordInput) {
        keywordInput.value = "";
    }

    if (typeSelect) {
        typeSelect.value = "";
    }

    adminPostKeyword = "";
    adminPostType = "";
    adminPostCurrentPage = 0;

    loadAdminPosts(0);
}

/**
 * 검색 입력창 Enter 처리.
 */
function handleAdminPostSearchEnter(event) {
    if (event.key === "Enter") {
        searchAdminPosts();
    }
}

/**
 * 새로고침.
 */
function reloadAdminPosts() {
    loadAdminPostSummary();
    loadAdminPosts(adminPostCurrentPage);
}

/**
 * 페이지 크기 변경.
 */
function changeAdminPostPageSize() {
    const sizeSelect =
        document.getElementById("adminPostSizeSelect");

    const selectedSize =
        Number(sizeSelect?.value ?? 10);

    adminPostPageSize =
        Number.isFinite(selectedSize)
            ? selectedSize
            : 10;

    adminPostCurrentPage = 0;

    loadAdminPosts(0);
}

/**
 * 게시글 삭제.
 */
async function deleteAdminPost(postId, title) {
    const displayTitle =
        title && title.trim()
            ? title
            : `게시글 ${postId}`;

    const confirmed = confirm(
        `"${displayTitle}" 게시글을 삭제하시겠습니까?\n\n` +
        "게시글에 연결된 댓글, 좋아요, 신고 데이터가 " +
        "함께 삭제될 수 있습니다."
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `/admin/posts/${postId}`,
            {
                method: "DELETE",
                headers: getAdminPostHeaders()
            }
        );

        if (response.status === 401 || response.status === 403) {
            alert("게시글을 삭제할 관리자 권한이 없습니다.");
            return;
        }

        if (response.status === 404) {
            alert("이미 삭제되었거나 존재하지 않는 게시글입니다.");
            reloadAdminPosts();
            return;
        }

        if (!response.ok) {
            let errorMessage = "게시글 삭제에 실패했습니다.";

            try {
                const errorBody = await response.json();

                errorMessage =
                    errorBody.message ??
                    errorBody.error ??
                    errorMessage;
            } catch (ignored) {
                // 응답 본문이 JSON이 아닌 경우 기본 메시지 사용
            }

            throw new Error(errorMessage);
        }

        alert("게시글이 삭제되었습니다.");

        /*
         * 현재 페이지에 게시글이 하나뿐이었다면
         * 삭제 후 이전 페이지로 이동.
         */
        const currentRows =
            document.querySelectorAll(
                "#adminPostTableBody tr"
            );

        if (
            currentRows.length === 1 &&
            adminPostCurrentPage > 0
        ) {
            adminPostCurrentPage--;
        }

        await Promise.all([
            loadAdminPostSummary(),
            loadAdminPosts(adminPostCurrentPage)
        ]);

    } catch (error) {
        console.error(error);
        alert(error.message ?? "게시글 삭제에 실패했습니다.");
    }
}

/**
 * 페이지네이션.
 */
function updateAdminPostPagination(pageData) {
    const totalPages = pageData.totalPages ?? 0;
    const currentPage = pageData.number ?? 0;
    const totalElements = pageData.totalElements ?? 0;

    adminPostTotalPages = totalPages;
    adminPostCurrentPage = currentPage;

    const pageInfo =
        document.getElementById("adminPostPageInfo");

    if (pageInfo) {
        const visibleCurrentPage =
            totalPages === 0 ? 0 : currentPage + 1;

        pageInfo.textContent =
            `${visibleCurrentPage} / ${totalPages}` +
            ` (총 ${formatNumber(totalElements)}개)`;
    }

    const isFirst =
        pageData.first === true || currentPage <= 0;

    const isLast =
        pageData.last === true ||
        totalPages === 0 ||
        currentPage >= totalPages - 1;

    setButtonDisabled(
        "adminPostFirstBtn",
        isFirst
    );

    setButtonDisabled(
        "adminPostPrevBtn",
        isFirst
    );

    setButtonDisabled(
        "adminPostNextBtn",
        isLast
    );

    setButtonDisabled(
        "adminPostLastBtn",
        isLast
    );
}

function goFirstAdminPostPage() {
    if (adminPostCurrentPage > 0) {
        loadAdminPosts(0);
    }
}

function prevAdminPostPage() {
    if (adminPostCurrentPage > 0) {
        loadAdminPosts(adminPostCurrentPage - 1);
    }
}

function nextAdminPostPage() {
    if (
        adminPostCurrentPage + 1 <
        adminPostTotalPages
    ) {
        loadAdminPosts(adminPostCurrentPage + 1);
    }
}

function goLastAdminPostPage() {
    if (adminPostTotalPages > 0) {
        loadAdminPosts(adminPostTotalPages - 1);
    }
}

/**
 * 화면 보조 함수.
 */
function setAdminPostLoading() {
    const tableBody =
        document.getElementById("adminPostTableBody");

    if (!tableBody) {
        return;
    }

    tableBody.innerHTML = `
        <tr>
            <td colspan="11">
                게시글을 불러오는 중입니다.
            </td>
        </tr>
    `;
}

function getAdminPostTypeBadge(type) {
    if (type === "FREE") {
        return `
            <span class="status-badge normal">
                자유
            </span>
        `;
    }

    if (type === "IMPROVEMENT") {
        return `
            <span class="status-badge warning">
                개선 요청
            </span>
        `;
    }

    return `
        <span class="status-badge">
            ${escapeAdminPostHtml(type ?? "-")}
        </span>
    `;
}

function createAdminPostFileCell(fileUrl) {
    if (!fileUrl) {
        return "-";
    }

    const safeUrl =
        escapeAdminPostHtml(fileUrl);

    return `
        <a
            href="${safeUrl}"
            target="_blank"
            rel="noopener noreferrer">
            파일 보기
        </a>
    `;
}

function formatAdminPostDate(dateValue) {
    if (!dateValue) {
        return "-";
    }

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return escapeAdminPostHtml(dateValue);
    }

    return date.toLocaleString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function formatNumber(value) {
    const numberValue = Number(value ?? 0);

    if (!Number.isFinite(numberValue)) {
        return "0";
    }

    return numberValue.toLocaleString("ko-KR");
}

function setText(elementId, value) {
    const element =
        document.getElementById(elementId);

    if (element) {
        element.textContent = value;
    }
}

function setButtonDisabled(elementId, disabled) {
    const button =
        document.getElementById(elementId);

    if (button) {
        button.disabled = disabled;
    }
}

function escapeAdminPostHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeAdminPostJsString(value) {
    return String(value ?? "")
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'")
        .replaceAll("\"", "\\&quot;")
        .replaceAll("\n", " ")
        .replaceAll("\r", " ");
}