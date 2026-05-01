let paymentCurrentPage = 0;
let paymentTotalPages = 1;

const paymentPageSize = 20;

async function loadPayments() {
    const url = `/admin/payments?page=${paymentCurrentPage}&size=${paymentPageSize}`;

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
        const response = await fetch(`/admin/payments?page=${page}&size=${size}`, {
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

function updatePaymentSummary(payments) {
    const successStates = ["PURCHASED", "SUCCESS", "PAID"];
    const failedStates = ["CANCELED", "FAILED", "REFUNDED"];

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
        tbody.innerHTML = `<tr><td colspan="9" class="empty">조회된 결제 내역이 없습니다.</td></tr>`;
    } else {
        payments.forEach(payment => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td class="code">${shortText(payment.purchaseToken)}</td>
                <td>${payment.memberId ?? "-"}</td>
                <td>${payment.memberEmail ?? "-"}</td>
                <td>${payment.memberNickname ?? "-"}</td>
                <td>${payment.productName ?? "-"}</td>
                <td>${formatPrice(payment.amount, payment.currency)}</td>
                <td>${payment.purchaseType ?? "-"}</td>
                <td>${purchaseStateBadge(payment.purchaseState)}</td>
                <td>${formatDateTime(payment.purchaseTime)}</td>
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

    if (state === "PURCHASED" || state === "SUCCESS" || state === "PAID") {
        return `<span class="badge green">${state}</span>`;
    }

    if (state === "PENDING") {
        return `<span class="badge blue-badge">${state}</span>`;
    }

    if (state === "CANCELED" || state === "FAILED" || state === "REFUNDED") {
        return `<span class="badge red">${state}</span>`;
    }

    return `<span class="badge gray">${state}</span>`;
}