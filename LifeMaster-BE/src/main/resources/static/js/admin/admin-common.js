window.onload = function () {
    const token = localStorage.getItem("accessToken");

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "/admin/login";
        return;
    }

    loadCoupons();
};

function getToken() {
    return localStorage.getItem("accessToken");
}

function authHeaders() {
    return {
        "Authorization": "Bearer " + getToken()
    };
}

async function handleAuthError(response) {
    if (response.status === 401) {
        alert("로그인이 만료되었습니다. 다시 로그인하세요.");
        localStorage.removeItem("accessToken");
        location.href = "/admin/login";
        return true;
    }

    if (response.status === 403) {
        alert("관리자 권한이 필요합니다.");
        return true;
    }

    return false;
}

function showPage(page, element) {
    document.querySelectorAll(".page-section").forEach(section => {
        section.style.display = "none";
    });

    document.querySelectorAll(".nav-item").forEach(item => {
        item.classList.remove("active");
    });

    element.classList.add("active");

    if (page === "coupons") {
        document.getElementById("page-coupons").style.display = "block";
        document.getElementById("pageTitle").textContent = "Coupon Dashboard";
        loadCoupons();
    }

    if (page === "payments") {
        document.getElementById("page-payments").style.display = "block";
        document.getElementById("pageTitle").textContent = "Payment Dashboard";
        paymentCurrentPage = 0;
        loadPayments();
        loadPaymentSummary();
    }

    if (page === "premium") {
        document.getElementById("page-premium").style.display = "block";
        document.getElementById("pageTitle").textContent = "Premium Member Dashboard";

        loadPremiumMembers();
        loadNonPremiumMembers(0);
    }

    if (page === "reports") {
        document.getElementById("page-reports").style.display = "block";
        document.getElementById("pageTitle").textContent = "Report Management Dashboard";
        reportCurrentPage = 0;
        loadReports();
        loadReportSummary();
    }

    if (page === "members") {
        document.getElementById("page-members").style.display = "block";
        document.getElementById("pageTitle").textContent = "Member Management Dashboard";

        memberCurrentPage = 0;
        loadMembers();
        loadMemberSummary();
    }

    if (page === "stats") {
        document.getElementById("page-stats").style.display = "block";
        document.getElementById("pageTitle").textContent = "Statistics Dashboard";
        loadStats();
    }
}

function logout() {
    localStorage.removeItem("accessToken");
    location.href = "/admin/login";
}

function formatDate(value) {
    if (!value) return "-";

    const date = new Date(value);
    if (isNaN(date.getTime())) return value;

    return date.toLocaleDateString("ko-KR");
}

function formatDateTime(value) {
    if (!value) return "-";

    const date = new Date(value);
    if (isNaN(date.getTime())) return value;

    return date.toLocaleString("ko-KR");
}

function formatPrice(amount, currency) {
    if (amount === null || amount === undefined) return "-";

    const formatted = Number(amount).toLocaleString("ko-KR");

    if (currency) {
        return `${formatted} ${currency}`;
    }

    return `${formatted}원`;
}

function shortText(value) {
    if (!value) return "-";
    if (value.length <= 16) return value;

    return value.substring(0, 16) + "...";
}