-- 외래 키 제약 해제
SET FOREIGN_KEY_CHECKS = 0;

-- 🔽 자식 테이블부터 삭제 (외래 키 순서 고려)
DELETE FROM `vote_entity$vote`;
DELETE FROM `vote_entity$poll_option`;
DELETE FROM `vote_entity$poll`;

DELETE FROM `comment_entity`;
DELETE FROM `post_entity`;
DELETE FROM `todo_entity`;
DELETE FROM `schedule_calendar_entity`;
DELETE FROM `member_entity`;
DELETE FROM `payment_entity`;
DELETE FROM `oauth_users_entity`;
DELETE FROM `group_entity`;
DELETE FROM `goal_entity`;
DELETE FROM `group_statistics_goals`;
DELETE FROM `member_group`;

-- 🔄 AUTO_INCREMENT 초기화
ALTER TABLE `member_entity` AUTO_INCREMENT = 1;
ALTER TABLE `group_entity` AUTO_INCREMENT = 1;
ALTER TABLE `goal_entity` AUTO_INCREMENT = 1;
ALTER TABLE `oauth_users_entity` AUTO_INCREMENT = 1;

-- 외래 키 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;