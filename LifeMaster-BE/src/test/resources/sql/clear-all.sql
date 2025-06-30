-- 외래 키 제약 해제
SET FOREIGN_KEY_CHECKS = 0;

-- 자식 테이블부터 삭제 (외래 키 순서 고려)
DELETE FROM `comment_entity`;
DELETE FROM `post_entity`;
DELETE FROM `todo_entity`;
DELETE FROM `member_entity`;
DELETE FROM `payment_entity`;
DELETE FROM `oauth_users_entity`;

-- 외래 키 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;