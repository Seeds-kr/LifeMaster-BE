async function loadStats() {

    // 활성 유저
    const activeRes = await fetch("/admin/stats/active-users", {
        headers: authHeaders()
    });
    if (await handleAuthError(activeRes)) return;

    const activeData = await activeRes.json();
    document.getElementById("activeUsers").innerText = activeData.activeUsers;

    // 오늘 가입자
    const userRes = await fetch("/admin/stats/users/daily", {
        headers: authHeaders()
    });
    const users = await userRes.json();
    document.getElementById("todayUsers").innerText =
        users.length ? users[users.length - 1].count : 0;

    // 오늘 게시글
    const postRes = await fetch("/admin/stats/posts/daily", {
        headers: authHeaders()
    });
    const posts = await postRes.json();
    document.getElementById("todayPosts").innerText =
        posts.length ? posts[posts.length - 1].count : 0;

    // 오늘 댓글
    const commentRes = await fetch("/admin/stats/comments/daily", {
        headers: authHeaders()
    });
    const comments = await commentRes.json();
    document.getElementById("todayComments").innerText =
        comments.length ? comments[comments.length - 1].count : 0;

    // 인기 게시글
    const popularRes = await fetch("/admin/stats/popular-posts", {
        headers: authHeaders()
    });
    const popular = await popularRes.json();

    const tbody = document.getElementById("popularPostTable");
    tbody.innerHTML = "";

    popular.forEach(p => {
        tbody.innerHTML += `
            <tr>
                <td>${p.postId}</td>
                <td>${p.title}</td>
                <td>${p.viewCount}</td>
                <td>${p.commentCount}</td>
                <td>${p.likeCount}</td>
                <td>${p.score}</td>
            </tr>
        `;
    });

    // 로그인 타입
    const loginRes = await fetch("/admin/stats/users/login-type", {
        headers: authHeaders()
    });
    const loginData = await loginRes.json();

    const loginTable = document.getElementById("loginTypeTable");
    loginTable.innerHTML = "";

    loginData.forEach(l => {
        loginTable.innerHTML += `
            <tr>
                <td>${l.loginType}</td>
                <td>${l.count}</td>
            </tr>
        `;
    });

    // 게시글 타입
    const typeRes = await fetch("/admin/stats/posts/type", {
        headers: authHeaders()
    });
    const typeData = await typeRes.json();

    const postTypeTable = document.getElementById("postTypeTable");
    postTypeTable.innerHTML = "";

    typeData.forEach(t => {
        postTypeTable.innerHTML += `
            <tr>
                <td>${t.type}</td>
                <td>${t.count}</td>
            </tr>
        `;
    });
}