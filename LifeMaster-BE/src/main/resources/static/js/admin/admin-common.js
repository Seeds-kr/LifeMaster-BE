window.onload = function () {
    const token = localStorage.getItem("accessToken");

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "/admin/login";
        return;
    }

    if (typeof loadCoupons === "function") {
        loadCoupons();
    }
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

    if (element) {
        element.classList.add("active");
    }

    const pageTitle = document.getElementById("pageTitle");

    if (page === "coupons") {
        document.getElementById("page-coupons").style.display = "block";
        pageTitle.textContent = "Coupon Dashboard";

        if (typeof loadCoupons === "function") {
            loadCoupons();
        }
    }

    if (page === "payments") {
        document.getElementById("page-payments").style.display = "block";
        pageTitle.textContent = "Payment Dashboard";

        if (typeof paymentCurrentPage !== "undefined") {
            paymentCurrentPage = 0;
        }

        if (typeof loadPayments === "function") {
            loadPayments();
        }

        if (typeof loadPaymentSummary === "function") {
            loadPaymentSummary();
        }
    }

    if (page === "premium") {
        document.getElementById("page-premium").style.display = "block";
        pageTitle.textContent = "Premium Member Dashboard";

        if (typeof loadPremiumMembers === "function") {
            loadPremiumMembers();
        }

        if (typeof loadNonPremiumMembers === "function") {
            loadNonPremiumMembers(0);
        }
    }

    if (page === "reports") {
        document.getElementById("page-reports").style.display = "block";
        pageTitle.textContent = "Report Management Dashboard";

        if (typeof reportCurrentPage !== "undefined") {
            reportCurrentPage = 0;
        }

        if (typeof loadReports === "function") {
            loadReports();
        }

        if (typeof loadReportSummary === "function") {
            loadReportSummary();
        }
    }

    if (page === "members") {
        document.getElementById("page-members").style.display = "block";
        pageTitle.textContent = "Member Management Dashboard";

        if (typeof memberCurrentPage !== "undefined") {
            memberCurrentPage = 0;
        }

        if (typeof loadMembers === "function") {
            loadMembers();
        }

        if (typeof loadMemberSummary === "function") {
            loadMemberSummary();
        }
    }

    if (page === "groups") {
        document.getElementById("page-groups").style.display = "block";
        pageTitle.textContent = "Group Management Dashboard";

        if (typeof groupCurrentPage !== "undefined") {
            groupCurrentPage = 1;
        }

        if (typeof loadGroups === "function") {
            loadGroups();
        } else {
            console.error("loadGroups 함수가 없습니다. admin-groups.js 로드 여부를 확인하세요.");
        }
    }

    if (page === "stats") {
        document.getElementById("page-stats").style.display = "block";
        pageTitle.textContent = "Statistics Dashboard";

        if (typeof loadStats === "function") {
            loadStats();
        } else {
            console.error("loadStats 함수가 없습니다. admin-stats.js 로드 여부를 확인하세요.");
        }
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