function loadChallenges() {
    fetch('/challenge?page=0')
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("challengeTableBody");
            tbody.innerHTML = "";

            data.content.forEach(c => {
                tbody.innerHTML += `
                    <tr>
                        <td>${c.challId}</td>
                        <td><img src="${c.challImg}" width="50"/></td>
                        <td>${c.challName}</td>
                        <td>${c.challDesc}</td>
                        <td>${c.challCnt}</td>
                        <td>${c.challMe ? "YES" : "NO"}</td>
                        <td>
                            <button onclick="joinChallenge(${c.challId})">참여</button>
                            <button onclick="leaveChallenge(${c.challId})">탈퇴</button>
                        </td>
                    </tr>
                `;
            });
        });
}

function loadMyChallenges() {
    fetch('/challenge/my')
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("myChallengeTableBody");
            tbody.innerHTML = "";

            data.forEach(c => {
                tbody.innerHTML += `
                    <tr>
                        <td>${c.challId}</td>
                        <td>${c.challName}</td>
                        <td>${c.challDesc}</td>
                        <td>${c.challCnt}</td>
                        <td>참여중</td>
                    </tr>
                `;
            });
        });
}

function loadCompletedChallenges() {
    fetch('/challenge/complete')
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("challengeCompleteTableBody");
            tbody.innerHTML = "";

            data.forEach(c => {
                tbody.innerHTML += `
                    <tr>
                        <td>${c.challId}</td>
                        <td>${c.completed ? "완료" : "미완료"}</td>
                        <td>${c.completedAt || "-"}</td>
                    </tr>
                `;
            });
        });
}