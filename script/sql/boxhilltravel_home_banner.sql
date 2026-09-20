-- ----------------------------------------------------------------------------
-- 首页横幅 holidays_home_banner
-- 用于后台管理首页 Hero 轮播图片，可在管理端上传图片并配置展示顺序/状态。
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `holidays_home_banner` (
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `image`       varchar(500) DEFAULT NULL COMMENT '横幅图片(OSS id)',
    `alt_text`    varchar(255) DEFAULT NULL COMMENT '替代文本',
    `title`       varchar(255) DEFAULT NULL COMMENT '标题',
    `subtitle`    varchar(255) DEFAULT NULL COMMENT '副标题',
    `link_url`    varchar(500) DEFAULT NULL COMMENT '跳转链接',
    `sort_order`  int(4)       DEFAULT 0 COMMENT '展示顺序，数值越小越靠前',
    `status`      tinyint(1)   DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `create_dept` bigint(20)   DEFAULT NULL COMMENT '创建部门',
    `create_by`   bigint(20)   DEFAULT NULL COMMENT '创建者',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_by`   bigint(20)   DEFAULT NULL COMMENT '更新者',
    `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sort_status` (`status`, `sort_order`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = '首页横幅';

-- ----------------------------------------------------------------------------
-- 菜单：首页横幅（BoxHillTravel 管理端）
-- 采用幂等插入，parent 菜单不存在时会自动创建。
-- ----------------------------------------------------------------------------

-- 父级菜单：BoxHillTravel 管理（若已存在则跳过）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 1761400000001000001, 'BoxHillTravel管理', 0, 10, 'boxhilltravel_manager', NULL, '', 'N', 'Y', 'M', '0', '0', '', 'example', '', '', 1761000000000000103, 1761100000000000001, sysdate(), NULL, NULL, 'BoxHillTravel管理菜单'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'BoxHillTravel管理');

-- 菜单：首页横幅列表页
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 1761400000001000010, '首页横幅', (SELECT menu_id FROM sys_menu WHERE menu_name = 'BoxHillTravel管理'), 1, 'home_banner', 'boxhilltravel_manager/home_banner/index', '', 'N', 'Y', 'C', '0', '0', 'boxhilltravel_manager:home_banner:list', 'picture', '', '', 1761000000000000103, 1761100000000000001, sysdate(), NULL, NULL, '首页横幅菜单'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:list');

-- 按钮权限：查询/新增/修改/删除
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 1761400000001000011, '首页横幅查询', (SELECT menu_id FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:list'), 1, '#', '', '', 'N', 'Y', 'F', '0', '0', 'boxhilltravel_manager:home_banner:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), NULL, NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:query');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 1761400000001000012, '首页横幅新增', (SELECT menu_id FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:list'), 2, '#', '', '', 'N', 'Y', 'F', '0', '0', 'boxhilltravel_manager:home_banner:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), NULL, NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:add');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 1761400000001000013, '首页横幅修改', (SELECT menu_id FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:list'), 3, '#', '', '', 'N', 'Y', 'F', '0', '0', 'boxhilltravel_manager:home_banner:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), NULL, NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:edit');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 1761400000001000014, '首页横幅删除', (SELECT menu_id FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:list'), 4, '#', '', '', 'N', 'Y', 'F', '0', '0', 'boxhilltravel_manager:home_banner:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), NULL, NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'boxhilltravel_manager:home_banner:remove');