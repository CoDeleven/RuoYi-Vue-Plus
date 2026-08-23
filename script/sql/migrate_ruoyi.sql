-- Account security state for H5 customers.
-- Resolve any rows returned by these checks before adding unique indexes.
SELECT user_name, COUNT(*) AS duplicate_count
FROM sys_user
GROUP BY user_name
HAVING COUNT(*) > 1;

SELECT email, COUNT(*) AS duplicate_count
FROM sys_user
WHERE email IS NOT NULL AND email <> ''
GROUP BY email
HAVING COUNT(*) > 1;

SELECT auth_id, COUNT(*) AS duplicate_count
FROM sys_social
GROUP BY auth_id
HAVING COUNT(*) > 1;

ALTER TABLE sys_user
    ADD COLUMN email_verified tinyint(1) NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证' AFTER email,
    ADD COLUMN password_configured tinyint(1) NOT NULL DEFAULT 1 COMMENT '用户是否已主动设置密码' AFTER password;

UPDATE sys_user
SET email_verified = CASE WHEN email IS NOT NULL AND email <> '' THEN 1 ELSE 0 END;

UPDATE sys_user u
SET password_configured = CASE
    WHEN u.user_type = 'app_user' AND EXISTS (
        SELECT 1 FROM sys_social s WHERE s.user_id = u.user_id AND s.del_flag = '0'
    ) THEN 0
    ELSE 1
END;

ALTER TABLE sys_social
    ADD UNIQUE KEY uk_sys_social_auth_id (auth_id),
    ADD UNIQUE KEY uk_sys_social_user_source (user_id, source);
