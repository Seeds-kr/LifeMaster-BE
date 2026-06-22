let paymentCurrentPage = 0;
let paymentTotalPages = 1;

const paymentPageSize = 20;

let paymentSearchMemberId = "";
let paymentSearchEmail = "";

async function loadPayments() {
    let url = `/admin/payments?page=${paymentCurrentPage}&size=${paymentPageSize}`;

    if (paymentSearchMemberId) {
        url += `&memberId=${encodeURIComponent(paymentSearchMemberId)}`;
    } else if (paymentSearchEmail) {
        url += `&email=${encodeURIComponent(paymentSearchEmail)}`;
    }

    const response = await fetch(url, {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("결제 내역 조회 실패");
        return;
    }

    const data = await response.json();

    const payments = data.content ?? [];
    paymentCurrentPage = data.number ?? paymentCurrentPage;
    paymentTotalPages = data.totalPages ?? 1;

    renderPayments(payments);
}

async function loadPaymentSummary() {
    const size = 100;
    let page = 0;
    let totalPages = 1;
    let allPayments = [];

    while (page < totalPages) {
        let url = `/admin/payments?page=${page}&size=${size}`;

        if (paymentSearchMemberId) {
            url += `&memberId=${encodeURIComponent(paymentSearchMemberId)}`;
        } else if (paymentSearchEmail) {
            url += `&email=${encodeURIComponent(paymentSearchEmail)}`;
        }

        const response = await fetch(url, {
            method: "GET",
            headers: authHeaders()
        });

        if (await handleAuthError(response)) return;

        if (!response.ok) {
            return;
        }

        const data = await response.json();
        allPayments = allPayments.concat(data.content ?? []);

        totalPages = data.totalPages ?? 1;
        page++;
    }

    updatePaymentSummary(allPayments);
}

function searchPayments() {
    const memberIdInput = document.getElementById("paymentMemberIdInput");
    const emailInput = document.getElementById("paymentEmailInput");

    paymentSearchMemberId = memberIdInput.value.trim();
    paymentSearchEmail = emailInput.value.trim();

    if (paymentSearchMemberId && paymentSearchEmail) {
        alert("회원 ID와 이메일 중 하나만 입력하세요.");
        return;
    }

    paymentCurrentPage = 0;
    loadPayments();
    loadPaymentSummary();
}

function resetPaymentSearch() {
    document.getElementById("paymentMemberIdInput").value = "";
    document.getElementById("paymentEmailInput").value = "";

    paymentSearchMemberId = "";
    paymentSearchEmail = "";

    paymentCurrentPage = 0;
    loadPayments();
    loadPaymentSummary();
}

function updatePaymentSummary(payments) {
    const successStates = ["PURCHASED", "SUCCESS", "PAID", "COMPLETED"];
    const failedStates = ["CANCELED", "CANCELLED", "FAILED", "REFUNDED"];

    document.getElementById("paymentTotalCount").textContent = payments.length;

    document.getElementById("paymentSuccessCount").textContent =
        payments.filter(p => successStates.includes(p.purchaseState)).length;

    document.getElementById("paymentPendingCount").textContent =
        payments.filter(p => p.purchaseState === "PENDING").length;

    document.getElementById("paymentFailedCount").textContent =
        payments.filter(p => failedStates.includes(p.purchaseState)).length;
}

function reloadPayments() {
    paymentCurrentPage = 0;
    loadPayments();
    loadPaymentSummary();
}

function renderPayments(payments) {
    const tbody = document.getElementById("paymentTableBody");
    tbody.innerHTML = "";

    if (!payments || payments.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="10" class="empty">조회된 결제 내역이 없습니다.</td>
            </tr>
        `;
    } else {
        payments.forEach(payment => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td class="code">${escapeHtml(shortText(payment.purchaseToken))}</td>
                <td>${payment.memberId ?? "-"}</td>
                <td>${escapeHtml(payment.memberEmail ?? "-")}</td>
                <td>${escapeHtml(payment.memberNickname ?? "-")}</td>
                <td>${escapeHtml(payment.productName ?? "-")}</td>
                <td>${formatPrice(payment.amount, payment.currency)}</td>
                <td>${escapeHtml(payment.purchaseType ?? "-")}</td>
                <td>${purchaseStateBadge(payment.purchaseState)}</td>
                <td>${formatDateTime(payment.purchaseTime)}</td>
                <td>
                    <button
                            class="danger-btn"
                            onclick="deletePayment('${escapeJs(payment.purchaseToken)}')">
                        삭제
                    </button>
                </td>
            `;

            tbody.appendChild(tr);
        });
    }

    document.getElementById("paymentPageInfo").textContent =
        `${paymentCurrentPage + 1} / ${paymentTotalPages || 1}`;

    document.getElementById("paymentPrevBtn").disabled = paymentCurrentPage <= 0;

    document.getElementById("paymentNextBtn").disabled =
        paymentCurrentPage >= (paymentTotalPages || 1) - 1;
}

async function deletePayment(purchaseToken) {
    if (!purchaseToken) {
        alert("삭제할 결제 토큰이 없습니다.");
        return;
    }

    if (!confirm(`결제 내역을 삭제하시겠습니까?\n토큰: ${purchaseToken}`)) {
        return;
    }

    const response = await fetch(
        `/admin/payments/${encodeURIComponent(purchaseToken)}`,
        {
            method: "DELETE",
            headers: authHeaders()
        }
    );

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const message = await response.text();
        alert(message || "결제 내역 삭제 실패");
        return;
    }

    alert("결제 내역이 삭제되었습니다.");

    loadPayments();
    loadPaymentSummary();
}

function prevPaymentPage() {
    if (paymentCurrentPage > 0) {
        paymentCurrentPage--;
        loadPayments();
    }
}

function nextPaymentPage() {
    if (paymentCurrentPage < paymentTotalPages - 1) {
        paymentCurrentPage++;
        loadPayments();
    }
}

function purchaseStateBadge(state) {
    if (!state) return `<span class="badge gray">-</span>`;

    if (
        state === "PURCHASED" ||
        state === "SUCCESS" ||
        state === "PAID" ||
        state === "COMPLETED"
    ) {
        return `<span class="badge green">${escapeHtml(state)}</span>`;
    }

    if (state === "PENDING") {
        return `<span class="badge blue-badge">${escapeHtml(state)}</span>`;
    }

    if (
        state === "CANCELED" ||
        state === "CANCELLED" ||
        state === "FAILED" ||
        state === "REFUNDED"
    ) {
        return `<span class="badge red">${escapeHtml(state)}</span>`;
    }

    return `<span class="badge gray">${escapeHtml(state)}</span>`;
}

function escapeJs(value) {
    return String(value ?? "")
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'")
        .replaceAll('"', '\\"')
        .replaceAll("\n", "\\n")
        .replaceAll("\r", "\\r");
}