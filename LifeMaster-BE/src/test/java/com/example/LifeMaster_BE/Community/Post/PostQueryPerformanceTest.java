package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Post.Dto.AllPostsDto;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * 게시글 목록 조회 쿼리 성능 측정 테스트
 *
 * 데이터: Member 100명, Post 10만 건(FREE), PostLike ~55만 건
 * 측정: 실행 시간(ms), Hibernate 쿼리 수, EXPLAIN 실행 계획
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.show-sql=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostQueryPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(PostQueryPerformanceTest.class);

    private static final int MEMBER_COUNT = 100;
    private static final int POST_COUNT = 100_000;
    private static final int AVG_LIKES_PER_POST = 5;
    private static final Pageable TEST_PAGEABLE =
            PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

    @Autowired
    private PostService postService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    private boolean dataReady = false;

    private Statistics getHibernateStatistics() {
        Session session = entityManager.unwrap(Session.class);
        return session.getSessionFactory().getStatistics();
    }

    @BeforeAll
    void setupBulkData() throws Exception {
        log.info("========== 데이터 준비 시작 ==========");
        long start = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            // 기존 데이터 정리 (재실행 대비)
            conn.createStatement().executeUpdate("DELETE FROM `post_like`");
            conn.createStatement().executeUpdate("DELETE FROM `post_entity`");
            conn.createStatement().executeUpdate("DELETE FROM `member_entity`");
            conn.commit();
            log.info("기존 데이터 정리 완료");

            conn.setAutoCommit(false);
            insertMembers(conn);
            insertPosts(conn);
            insertLikes(conn);

            conn.commit();
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("데이터 준비 완료: {}ms (Member={}, Post={}, Like≈{})",
                elapsed, MEMBER_COUNT, POST_COUNT, POST_COUNT * AVG_LIKES_PER_POST);
        dataReady = true;
    }

    @Test
    @Order(0)
    @DisplayName("Stage 0: 인덱스 없음 - 풀스캔 기준선")
    void stage0_noIndex() throws Exception {
        Assumptions.assumeTrue(dataReady, "데이터 준비가 완료되지 않아 스킵");

        // 인덱스 제거
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP INDEX `idx_type` ON `post_entity`");
            stmt.executeUpdate("ALTER TABLE `post_like` DROP INDEX `uq_post_like_member_post`");
            log.info("Stage 0: 인덱스 제거 완료");
        }

        // EXPLAIN
        logExplain("Stage 0 - post_entity 조회",
                "EXPLAIN SELECT * FROM `post_entity` WHERE `type` = 'FREE' ORDER BY `created_at` DESC LIMIT 20");

        logExplain("Stage 0 - post_like 좋아요 확인",
                "EXPLAIN SELECT * FROM `post_like` WHERE `member_id` = 1 AND `post_id` IN (1, 2, 3, 4, 5)");

        // 앱 서버 단 측정
        Statistics stats = getHibernateStatistics();
        stats.clear();

        long start = System.nanoTime();
        Page<AllPostsDto> result = postService.getAllPosts(1L, PostType.FREE, TEST_PAGEABLE);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        long queryCount = stats.getQueryExecutionCount();

        log.info("===== Stage 0 결과 =====");
        log.info("조회 건수: {}", result.getNumberOfElements());
        log.info("전체 건수: {}", result.getTotalElements());
        log.info("실행 시간: {}ms", elapsed);
        log.info("Hibernate 쿼리 수: {}", queryCount);
        log.info("========================");

        Assertions.assertFalse(result.isEmpty(), "조회 결과가 비어있으면 안 됨");
    }

    @Test
    @Order(1)
    @DisplayName("Stage 1: 단일 인덱스 - type, (member_id, post_id)")
    void stage1_singleIndex() throws Exception {
        Assumptions.assumeTrue(dataReady, "데이터 준비가 완료되지 않아 스킵");

        // 단일 인덱스 추가
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE INDEX `idx_type` ON `post_entity` (`type`)");
            stmt.executeUpdate("ALTER TABLE `post_like` ADD CONSTRAINT `uq_post_like_member_post` UNIQUE (`member_id`, `post_id`)");
            log.info("Stage 1: 단일 인덱스 추가 완료");
        }

        // EXPLAIN
        logExplain("Stage 1 - post_entity 조회",
                "EXPLAIN SELECT * FROM `post_entity` WHERE `type` = 'FREE' ORDER BY `created_at` DESC LIMIT 20");

        logExplain("Stage 1 - post_like 좋아요 확인",
                "EXPLAIN SELECT * FROM `post_like` WHERE `member_id` = 1 AND `post_id` IN (1, 2, 3, 4, 5)");

        // 앱 서버 단 측정
        Statistics stats = getHibernateStatistics();
        stats.clear();

        long start = System.nanoTime();
        Page<AllPostsDto> result = postService.getAllPosts(1L, PostType.FREE, TEST_PAGEABLE);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        long queryCount = stats.getQueryExecutionCount();

        log.info("===== Stage 1 결과 =====");
        log.info("조회 건수: {}", result.getNumberOfElements());
        log.info("전체 건수: {}", result.getTotalElements());
        log.info("실행 시간: {}ms", elapsed);
        log.info("Hibernate 쿼리 수: {}", queryCount);
        log.info("========================");

        Assertions.assertFalse(result.isEmpty(), "조회 결과가 비어있으면 안 됨");
    }

    // ============ EXPLAIN 유틸 ============

    private void logExplain(String label, String explainSql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(explainSql)) {

            log.info("----- {} -----", label);
            while (rs.next()) {
                log.info("EXPLAIN | id={} | select_type={} | table={} | type={} | key={} | rows={} | Extra={}",
                        rs.getInt("id"),
                        rs.getString("select_type"),
                        rs.getString("table"),
                        rs.getString("type"),
                        rs.getString("key"),
                        rs.getLong("rows"),
                        rs.getString("Extra"));
            }
        }
    }

    // ============ JDBC Batch Insert ============

    private void insertMembers(Connection conn) throws SQLException {
        String sql = "INSERT INTO `member_entity` " +
                "(`id`, `email`, `password`, `nickname`, `login_type`, `login_status`, " +
                "`login_role`, `member_status`, `warning_count`, `subscription_plan`, " +
                "`payment_status`, `created_at`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= MEMBER_COUNT; i++) {
                ps.setLong(1, i);
                ps.setString(2, "perf_user_" + i + "@test.com");
                ps.setString(3, "password123");
                ps.setString(4, "perfUser" + i);
                ps.setString(5, "EMAIL");
                ps.setBoolean(6, true);
                ps.setString(7, "USER");
                ps.setString(8, "ACTIVE");
                ps.setInt(9, 0);
                ps.setString(10, "FREE");
                ps.setString(11, "UNPAID");
                ps.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
                ps.addBatch();

                if (i % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
        log.info("Member {} 건 삽입 완료", MEMBER_COUNT);
    }

    private void insertPosts(Connection conn) throws SQLException {
        String sql = "INSERT INTO `post_entity` " +
                "(`id`, `title`, `content`, `type`, `view_count`, `comment_count`, " +
                "`created_at`, `member_id`, `calendar_shared`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        int seed = 42;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= POST_COUNT; i++) {
                ps.setLong(1, i);
                ps.setString(2, "제목 " + i);
                ps.setString(3, "내용 " + i);
                ps.setString(4, "FREE");
                ps.setInt(5, (i * 7 + seed) % 1000);
                ps.setInt(6, (i * 3 + seed) % 20);
                ps.setTimestamp(7, Timestamp.valueOf(baseTime.plusMinutes(i)));
                ps.setLong(8, (i % MEMBER_COUNT) + 1);
                ps.setBoolean(9, false);
                ps.addBatch();

                if (i % 5000 == 0) {
                    ps.executeBatch();
                    if (i % 50000 == 0) log.info("Post 삽입 진행: {}/{}", i, POST_COUNT);
                }
            }
            ps.executeBatch();
        }
        log.info("Post {} 건 삽입 완료", POST_COUNT);
    }

    private void insertLikes(Connection conn) throws SQLException {
        String sql = "INSERT INTO `post_like` (`id`, `member_id`, `post_id`) VALUES (?, ?, ?)";

        long likeId = 1;
        int totalLikes = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int postId = 1; postId <= POST_COUNT; postId++) {
                int startMember = (postId % MEMBER_COUNT) + 1;
                int likeCount = (postId % (AVG_LIKES_PER_POST * 2)) + 1;
                for (int j = 0; j < likeCount; j++) {
                    long memberId = ((startMember + j - 1) % MEMBER_COUNT) + 1;
                    ps.setLong(1, likeId++);
                    ps.setLong(2, memberId);
                    ps.setLong(3, postId);
                    ps.addBatch();
                    totalLikes++;
                }

                if (postId % 5000 == 0) {
                    ps.executeBatch();
                    if (postId % 50000 == 0) log.info("Like 삽입 진행: post {}/{}", postId, POST_COUNT);
                }
            }
            ps.executeBatch();
        }
        log.info("PostLike {} 건 삽입 완료", totalLikes);
    }
}
