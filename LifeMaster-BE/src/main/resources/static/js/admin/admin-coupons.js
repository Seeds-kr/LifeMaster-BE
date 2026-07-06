let allCoupons = [];
let filteredCoupons = [];
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

    // 조회 시 현재 검색어도 같이 적용
    applyCouponSearch();

    updateCouponSummary(allCoupons);
}

function renderCoupons() {
    const tbody = document.getElementById("couponTableBody");
    tbody.innerHTML = "";

    const targetCoupons = filteredCoupons;

    const totalPages = Math.ceil(targetCoupons.length / couponPageSize) || 1;
    const start = (couponCurrentPage - 1) * couponPageSize;
    const end = start + couponPageSize;
    const pageCoupons = targetCoupons.slice(start, end);

    if (pageCoupons.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty">조회된 쿠폰이 없습니다.</td></tr>`;
    } else {
        pageCoupons.forEach(coupon => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${coupon.couponId ?? ""}</td>
                <td class="code">${coupon.couponCode ?? ""}</td>
                <td>${coupon.couponType ?? ""}</td>
                <td>${couponStatusBadge(coupon.couponStatus)}</td>
                <td>${couponMemberText(coupon)}</td>
                <td>
                    <button 
                        class="delete-btn"
                        onclick="deleteCoupon(${coupon.couponId})"
                        ${coupon.couponStatus !== "UNUSE" ? "disabled" : ""}
                    >
                        삭제
                    </button>
                </td>
            `;

            tbody.appendChild(tr);
        });
    }

    document.getElementById("couponPageInfo").textContent =
        `${couponCurrentPage} / ${totalPages}`;

    document.getElementById("couponPrevBtn").disabled =
        couponCurrentPage <= 1;

    document.getElementById("couponNextBtn").disabled =
        couponCurrentPage >= totalPages;
}

function applyCouponSearch() {
    const input = document.getElementById("couponSearchInput");

    const keyword = input
        ? input.value.trim().toUpperCase()
        : "";

    if (!keyword) {
        filteredCoupons = [...allCoupons];
    } else {
        filteredCoupons = allCoupons.filter(coupon => {
            const id = String(coupon.couponId ?? "");
            const code = String(coupon.couponCode ?? "").toUpperCase();

            return id === keyword || code.includes(keyword);
        });
    }

    couponCurrentPage = 1;
    renderCoupons();
}

function searchCoupons() {
    applyCouponSearch();
}

function resetCouponSearch() {
    const input = document.getElementById("couponSearchInput");
    if (input) {
        input.value = "";
    }

    filteredCoupons = [...allCoupons];
    couponCurrentPage = 1;
    renderCoupons();
}

function handleCouponSearchEnter(event) {
    if (event.key === "Enter") {
        searchCoupons();
    }
}

function couponMemberText(coupon) {
    if (coupon.couponStatus === "UNUSE" || !coupon.memberId) {
        return "미등록";
    }

    const email = coupon.memberEmail ?? "";
    const nickname = coupon.memberNickname ?? "";

    if (email && nickname) {
        return `${email} (${nickname})`;
    }

    return email || nickname || "-";
}

async function createCoupon() {
    const couponType = document.getElementById("couponTypeSelect").value;

    const response = await fetch("/coupon/admin", {
        method: "POST",
        headers: {
            ...authHeaders(),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            couponType: couponType,
            couponPercent: 0
        })
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const errorText = await response.text();
        alert("쿠폰 생성 실패\n" + errorText);
        return;
    }

    alert("쿠폰이 생성되었습니다.");

    await loadCoupons();
}

async function deleteCoupon(couponId) {
    const confirmed = confirm("이 쿠폰을 삭제하시겠습니까?");
    if (!confirmed) return;

    const response = await fetch(`/coupon/admin/${couponId}`, {
        method: "DELETE",
        headers: authHeaders()
    });

    if (await handleAuthError(response)) return;

    if (!response.ok) {
        const errorText = await response.text();
        alert("쿠폰 삭제 실패\n" + errorText);
        return;
    }

    alert("쿠폰이 삭제되었습니다.");

    await loadCoupons();
}

function prevCouponPage() {
    if (couponCurrentPage > 1) {
        couponCurrentPage--;
        renderCoupons();
    }
}

function nextCouponPage() {
    const totalPages = Math.ceil(filteredCoupons.length / couponPageSize) || 1;

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