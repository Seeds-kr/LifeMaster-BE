let allCoupons = [];
let couponCurrentPage = 1;

const couponPageSize = 10;

async function loadCoupons() {
    const status = document.getElementById("statusSelect").value;

    let url = "/admin/coupons";
    if (status) {
        url += "?status=" + status;
    }

    const response = await fetch(url, {
        method: "GET",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        alert("쿠폰 조회 실패");
        return;
    }

    allCoupons = await response.json();
    couponCurrentPage = 1;

    renderCoupons();
    updateCouponSummary(allCoupons);
}

function renderCoupons() {
    const tbody = document.getElementById("couponTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(allCoupons.length / couponPageSize) || 1;
    const start = (couponCurrentPage - 1) * couponPageSize;
    const end = start + couponPageSize;
    const pageCoupons = allCoupons.slice(start, end);

    if (pageCoupons.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty">조회된 쿠폰이 없습니다.</td></tr>`;
    } else {
        pageCoupons.forEach(coupon => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${coupon.couponId ?? ""}</td>
                <td class="code">${coupon.couponCode ?? ""}</td>
                <td>${coupon.couponPercent ?? ""}%</td>
                <td>${coupon.couponType ?? ""}</td>
                <td>${couponStatusBadge(coupon.couponStatus)}</td>
                <td>${coupon.email ?? coupon.nickname ?? "-"}</td>
            `;

            tbody.appendChild(tr);
        });
    }

    document.getElementById("couponPageInfo").textContent = `${couponCurrentPage} / ${totalPages}`;
    document.getElementById("couponPrevBtn").disabled = couponCurrentPage <= 1;
    document.getElementById("couponNextBtn").disabled = couponCurrentPage >= totalPages;
}

function prevCouponPage() {
    if (couponCurrentPage > 1) {
        couponCurrentPage--;
        renderCoupons();
    }
}

function nextCouponPage() {
    const totalPages = Math.ceil(allCoupons.length / couponPageSize) || 1;

    if (couponCurrentPage < totalPages) {
        couponCurrentPage++;
        renderCoupons();
    }
}

function updateCouponSummary(coupons) {
    document.getElementById("totalCount").textContent = coupons.length;
    document.getElementById("unusedCount").textContent =
        coupons.filter(c => c.couponStatus === "UNUSE").length;
    document.getElementById("registeredCount").textContent =
        coupons.filter(c => c.couponStatus === "REGISTER").length;
    document.getElementById("usedCount").textContent =
        coupons.filter(c => c.couponStatus === "USE").length;
}

function couponStatusBadge(status) {
    if (status === "UNUSE") return `<span class="badge gray">UNUSE</span>`;
    if (status === "REGISTER") return `<span class="badge blue-badge">REGISTER</span>`;
    if (status === "USE") return `<span class="badge green">USE</span>`;

    return `<span class="badge gray">${status ?? "-"}</span>`;
}